/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.IteratorMember;
import org.openzen.zenscript.codemodel.member.DefinitionMember;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.member.StaticInitializerMember;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageTag;
import org.openzen.zenscript.javasource.scope.JavaSourceFileScope;
import org.openzen.zenscript.javashared.JavaMethod;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaExpansionMemberCompiler extends BaseMemberCompiler {
	private final List<FieldMember> fields = new ArrayList<>();
	private final TypeParameter[] expansionTypeParameters;
	
	public JavaExpansionMemberCompiler(
			JavaSourceFormattingSettings settings,
			TypeID targetType,
			TypeParameter[] expansionTypeParameters,
			String indent,
			StringBuilder output,
			JavaSourceFileScope scope,
			HighLevelDefinition definition)
	{
		super(settings, indent, output, scope, targetType, definition);
		
		this.expansionTypeParameters = expansionTypeParameters;
	}
	
	private void compileMethod(DefinitionMember member, FunctionHeader header, Statement body) {
		JavaMethod method = scope.context.getJavaMethod(member);
		if (!method.compile)
			return;
		
		begin(ElementType.METHOD);
		output.append(indent);
		
		modifiers(member.getEffectiveModifiers() | Modifiers.STATIC);
		if (member.isStatic())
			JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, header.typeParameters, true);
		else
			JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, expansionTypeParameters, header.typeParameters);
		
		output.append(scope.typeVisitor.process(header.getReturnType()));
		output.append(" ");
		output.append(method.name);
		formatParameters(member.isStatic(), expansionTypeParameters, header);
		compileBody(body, header);
	}
	
	@Override
	public Void visitConst(ConstMember member) {
		begin(ElementType.FIELD);
		modifiers(member.getEffectiveModifiers() | Modifiers.STATIC | Modifiers.FINAL);
		output.append(scope.type(member.getType()));
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
		
		output.append(scope.type(member.getType()));
		output.append(" ");
		output.append(member.name);
		if (member.initializer != null) {
			output.append(" = ");
			output.append(fieldInitializerScope.expression(null, member.initializer)); // TODO: formatting target
		}
		output.append(";\n");
		
		fields.add(member);
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		compileMethod(member, member.header, member.body);
		return null;
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		return null; // ignore
	}

	@Override
	public Void visitMethod(MethodMember member) {
		compileMethod(member, member.header, member.body);
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		compileMethod(member, new FunctionHeader(member.getType()), member.body);
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		compileMethod(member, new FunctionHeader(BasicTypeID.VOID, new FunctionParameter(member.getType(), "value")), member.body);
		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		compileMethod(member, member.header, member.body);
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		compileMethod(member, member.header, member.body);
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
		// TODO
		return null;
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember member) {
		// TODO
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		begin(ElementType.STATICINIT);
		output.append(indent).append("static ");
		compileBody(member.body, new FunctionHeader(BasicTypeID.VOID));
		return null;
	}
	
	public void finish() {
		// TODO: needs to be moved elsewhere (normalization stage?)
		for (FieldMember field : fields) {
			if (field.autoGetter != null)
				visitGetter(field.autoGetter);
			if (field.autoSetter != null)
				visitSetter(field.autoSetter);
		}
	}
}
