/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import java.util.Collections;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationInput;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class NativeAnnotationDefinition implements AnnotationDefinition {
	public static final NativeAnnotationDefinition INSTANCE = new NativeAnnotationDefinition();
	
	private static final List<FunctionHeader> INITIALIZERS = Collections.singletonList(
			new FunctionHeader(BasicTypeID.VOID, BasicTypeID.STRING));
	
	private NativeAnnotationDefinition() {}

	@Override
	public String getAnnotationName() {
		return "Native";
	}

	@Override
	public List<FunctionHeader> getInitializers(BaseScope scope) {
		return INITIALIZERS;
	}

	@Override
	public ExpressionScope getScopeForMember(IDefinitionMember member, BaseScope scope) {
		return new ExpressionScope(scope);
	}

	@Override
	public ExpressionScope getScopeForType(HighLevelDefinition definition, BaseScope scope) {
		return new ExpressionScope(scope);
	}

	@Override
	public ExpressionScope getScopeForStatement(Statement statement, StatementScope scope) {
		return new ExpressionScope(scope);
	}

	@Override
	public ExpressionScope getScopeForParameter(FunctionHeader header, FunctionParameter parameter, BaseScope scope) {
		return new ExpressionScope(scope);
	}

	@Override
	public MemberAnnotation createForMember(CodePosition position, CallArguments arguments) {
		Expression value = arguments.arguments[0];
		String constant = value.evaluateStringConstant();
		return new NativeMemberAnnotation(constant);
	}

	@Override
	public DefinitionAnnotation createForDefinition(CodePosition position, CallArguments arguments) {
		Expression value = arguments.arguments[0];
		String constant = value.evaluateStringConstant();
		return new NativeDefinitionAnnotation(constant);
	}

	@Override
	public StatementAnnotation createForStatement(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public Annotation createForParameter(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public MemberAnnotation deserializeForMember(CodeSerializationInput input, TypeContext context, IDefinitionMember member) {
		String name = input.readString();
		return new NativeMemberAnnotation(name);
	}

	@Override
	public DefinitionAnnotation deserializeForDefinition(CodeSerializationInput input, TypeContext context) {
		String name = input.readString();
		return new NativeDefinitionAnnotation(name);
	}

	@Override
	public StatementAnnotation deserializeForStatement(CodeSerializationInput input, StatementContext context) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public Annotation deserializeForParameter(CodeSerializationInput input, TypeContext context) {
		throw new UnsupportedOperationException("Not supported");
	}
}
