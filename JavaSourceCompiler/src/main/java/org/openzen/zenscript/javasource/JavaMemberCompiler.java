/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.javashared.JavaTypeNameVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.IteratorMember;
import org.openzen.zenscript.codemodel.member.DefinitionMember;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.member.StaticInitializerMember;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageTag;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.javasource.scope.JavaSourceFileScope;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaImplementation;
import org.openzen.zenscript.javashared.JavaMethod;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaMemberCompiler extends BaseMemberCompiler {
	private final JavaSourceCompiler compiler;
	private final JavaSourceFile file;
	private final List<FieldMember> fields = new ArrayList<>();
	private final boolean isInterface;
	private final Map<HighLevelDefinition, SemanticModule> modules;
	public boolean hasDestructor = false;
	
	public JavaMemberCompiler(
			JavaSourceCompiler compiler,
			JavaSourceFile file,
			JavaSourceFormattingSettings settings,
			String indent,
			StringBuilder output,
			JavaSourceFileScope scope,
			boolean isInterface,
			HighLevelDefinition definition,
			Map<HighLevelDefinition, SemanticModule> modules)
	{
		super(settings, indent, output, scope, null, definition);
		
		this.file = file;
		this.isInterface = isInterface;
		this.compiler = compiler;
		this.modules = modules;
	}
	
	private void compileMethod(DefinitionMember member, FunctionHeader header, Statement body) {
		JavaMethod method = member.getTag(JavaMethod.class);
		if (method == null)
			throw new AssertionError();
		if (!method.compile)
			return;
		
		boolean hasBody = body != null && !(body instanceof EmptyStatement);
		if (isInterface && method.name.equals("toString") && header.parameters.length == 0) {
			hasBody = false;
		}
		
		begin(ElementType.METHOD);
		override(member.getOverrides() != null);
		output.append(indent);
		if (isInterface && hasBody)
			output.append("default ");
		
		modifiers(member.getEffectiveModifiers());
		JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, header.typeParameters, true);
		output.append(scope.typeVisitor.process(header.getReturnType()));
		output.append(" ");
		output.append(method.name);
		formatParameters(member.isStatic(), header);
		
		if (hasBody)
			compileBody(body, header);
		else
			output.append(";\n");
	}
	
	@Override
	public Void visitConst(ConstMember member) {
		begin(ElementType.FIELD);
		
		output.append(indent);
		modifiers(member.getEffectiveModifiers() | Modifiers.STATIC | Modifiers.FINAL);
		output.append(scope.type(member.type));
		output.append(" ");
		output.append(member.name);
		output.append(" = ");
		output.append(fieldInitializerScope.expression(null, member.value));
		output.append(";\n");
		return null;
	}

	@Override
	public Void visitField(FieldMember member) {
		begin(ElementType.FIELD);
		
		output.append(indent);
		int modifiers = 0;
		if (member.isStatic())
			modifiers |= Modifiers.STATIC;
		if (member.isFinal())
			modifiers |= Modifiers.FINAL;
		if (member.autoGetterAccess != 0 && (member.isFinal() ? member.autoSetterAccess == 0 : member.autoGetterAccess == member.autoSetterAccess))
			modifiers |= member.autoGetterAccess;
		else
			modifiers |= Modifiers.PRIVATE;
		
		this.modifiers(modifiers);
		
		output.append(scope.type(member.type));
		output.append(" ");
		output.append(member.name);
		if (member.initializer != null) {
			output.append(" = ");
			output.append(fieldInitializerScope.expression(null, member.initializer)); // TODO: duplicable expressions -> initializer helpers!
		}
		output.append(";\n");
		
		fields.add(member);
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		begin(ElementType.CONSTRUCTOR);
		
		output.append(indent);
		modifiers(member.getEffectiveModifiers());
		JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, member.header.typeParameters, true);
		output.append(scope.cls.getName());
		formatParameters(member.isStatic(), member.header);
		compileBody(member.body, member.header);
		return null;
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		hasDestructor = true;
		begin(ElementType.METHOD);
		
		output.append(indent).append("@Override\n");
		output.append(indent).append("public void close()");
		
		Statement body = member.body;
		if ((body == null || body instanceof EmptyStatement) && !(definition instanceof InterfaceDefinition))
			body = new BlockStatement(member.position, new Statement[0]);
		
		compileBody(body, member.header);
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		compileMethod(member, member.header, member.body);
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		compileMethod(member, new FunctionHeader(member.type), member.body);
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		compileMethod(member, new FunctionHeader(BasicTypeID.VOID, new FunctionParameter(member.type, "value")), member.body);
		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		compileMethod(member, member.header, member.body);
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		compileMethod(member, new FunctionHeader(member.toType), member.body);
		return null;
	}

	@Override
	public Void visitCustomIterator(IteratorMember member) {
		compileMethod(member, new FunctionHeader(scope.semanticScope.getTypeRegistry().getIterator(member.getLoopVariableTypes()).stored(UniqueStorageTag.INSTANCE)), member.body);
		return null;
	}

	@Override
	public Void visitCaller(CallerMember member) {
		compileMethod(member, member.header, member.body);
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember member) {
		JavaImplementation implementation = member.getTag(JavaImplementation.class);
		if (implementation.inline) {
			for (IDefinitionMember m : member.members) {
				m.accept(this);
			}
		} else {
			String interfaceName = JavaTypeNameVisitor.INSTANCE.process(member.type);
			String implementationName = interfaceName + "Implementation";
			
			begin(ElementType.FIELD);
			output.append(indent);
			modifiers(member.getEffectiveModifiers());
			output.append("final ").append(scope.type(member.type)).append(" as").append(interfaceName).append(" = new ").append(implementationName).append("();\n");
			
			begin(ElementType.INNERCLASS);
			output.append("private class ").append(implementationName).append(" implements ").append(scope.type(member.type)).append(" {\n");
			JavaMemberCompiler memberCompiler = new JavaMemberCompiler(compiler, file, settings, indent + settings.indent, output, scope, isInterface, definition, modules);
			for (IDefinitionMember m : member.members) {
				m.accept(memberCompiler);
			}
			output.append(indent).append("}\n");
		}
		return null;
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember member) {
		JavaClass cls = member.innerDefinition.getTag(JavaClass.class);
		JavaDefinitionVisitor visitor = new JavaDefinitionVisitor(
				indent,
				compiler,
				cls,
				file,
				output,
				Collections.emptyList(),
				modules);
		member.innerDefinition.accept(visitor);
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		begin(ElementType.STATICINIT);
		output.append(indent).append("static");
		compileBody(member.body, new FunctionHeader(BasicTypeID.VOID));
		return null;
	}
	
	public void finish() {
		
	}
}
