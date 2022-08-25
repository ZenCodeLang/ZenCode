/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.javashared.JavaTypeNameVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.IteratorMember;
import org.openzen.zenscript.codemodel.member.DefinitionMember;
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
import org.openzen.zenscript.javasource.scope.JavaSourceFileScope;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaImplementation;
import org.openzen.zenscript.javashared.JavaNativeMethod;

/**
 * @author Hoofdgebruiker
 */
public class JavaMemberCompiler extends BaseMemberCompiler {
	private final JavaSourceCompiler compiler;
	private final JavaSourceFile file;
	private final List<FieldMember> fields = new ArrayList<>();
	private final boolean isInterface;
	private final JavaSourceContext context;
	private final JavaCompiledModule module;
	private final SemanticModule semanticModule;
	public boolean hasDestructor = false;

	public JavaMemberCompiler(
			JavaSourceCompiler compiler,
			JavaSourceContext context,
			JavaCompiledModule module,
			JavaSourceFile file,
			JavaSourceFormattingSettings settings,
			String indent,
			StringBuilder output,
			JavaSourceFileScope scope,
			boolean isInterface,
			HighLevelDefinition definition,
			SemanticModule semanticModule) {
		super(settings, indent, output, scope, null, definition);

		this.file = file;
		this.isInterface = isInterface;
		this.compiler = compiler;
		this.context = context;
		this.module = module;
		this.semanticModule = semanticModule;
	}

	private void compileMethod(DefinitionMember member, FunctionHeader header, Statement body) {
		JavaNativeMethod method = module.getMethodInfo(member);
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
	public Void visitField(FieldMember member) {
		begin(ElementType.FIELD);

		output.append(indent);
		Modifiers fieldModifiers = member.getEffectiveModifiers();
		Modifiers modifiers = Modifiers.NONE;
		if (fieldModifiers.isStatic() || fieldModifiers.isConst())
			modifiers = modifiers.withStatic();
		if (fieldModifiers.isFinal() || fieldModifiers.isFinal())
			modifiers = modifiers.withFinal();
		if (member.autoGetterAccess != 0 && (member.isFinal() ? member.autoSetterAccess == 0 : member.autoGetterAccess == member.autoSetterAccess))
			modifiers |= member.autoGetterAccess;
		else
			modifiers = modifiers.withPrivate();

		this.modifiers(modifiers);

		output.append(scope.type(member.getType()));
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
		if (member.body == null)
			return null; // happens with default constructors

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
	public Void visitMethod(MethodMember member) {
		compileMethod(member, member.header, member.body);
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		compileMethod(member, member.header, member.body);
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		compileMethod(member, member.header, member.body);
		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		if (member.operator == OperatorType.DESTRUCTOR) {
			hasDestructor = true;
			begin(ElementType.METHOD);

			output.append(indent).append("@Override\n");
			output.append(indent).append("public void close()");

			Statement body = member.body;
			if ((body == null || body instanceof EmptyStatement) && !(definition instanceof InterfaceDefinition))
				body = new BlockStatement(member.position, new Statement[0]);

			compileBody(body, member.header);
		} else {
			compileMethod(member, member.header, member.body);
		}
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		compileMethod(member, new FunctionHeader(member.toType), member.body);
		return null;
	}

	@Override
	public Void visitCustomIterator(IteratorMember member) {
		compileMethod(member, new FunctionHeader(new IteratorTypeID(member.getLoopVariableTypes())), member.body);
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember member) {
		JavaImplementation implementation = module.getImplementationInfo(member);
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
			JavaMemberCompiler memberCompiler = new JavaMemberCompiler(compiler, context, module, file, settings, indent + settings.indent, output, scope, isInterface, definition, semanticModule);
			for (IDefinitionMember m : member.members) {
				m.accept(memberCompiler);
			}
			output.append(indent).append("}\n");
		}
		return null;
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember member) {
		JavaClass cls = context.optJavaClass(member.innerDefinition);
		if (cls == null)
			return null;

		JavaDefinitionVisitor visitor = new JavaDefinitionVisitor(
				indent,
				compiler,
				context,
				module,
				cls,
				file,
				output,
				Collections.emptyList(),
				semanticModule);
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
