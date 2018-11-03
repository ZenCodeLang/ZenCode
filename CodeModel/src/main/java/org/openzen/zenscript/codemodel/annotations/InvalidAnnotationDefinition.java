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
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationInput;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public class InvalidAnnotationDefinition implements AnnotationDefinition {
	public static final InvalidAnnotationDefinition INSTANCE = new InvalidAnnotationDefinition();
	
	private InvalidAnnotationDefinition() {}

	@Override
	public String getAnnotationName() {
		return "Invalid";
	}

	@Override
	public List<FunctionHeader> getInitializers(BaseScope scope) {
		return Collections.emptyList();
	}

	@Override
	public ExpressionScope getScopeForMember(IDefinitionMember member, BaseScope scope) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public ExpressionScope getScopeForType(HighLevelDefinition definition, BaseScope scope) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public ExpressionScope getScopeForStatement(Statement statement, StatementScope scope) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public ExpressionScope getScopeForParameter(FunctionHeader header, FunctionParameter parameter, BaseScope scope) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public MemberAnnotation createForMember(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public DefinitionAnnotation createForDefinition(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public StatementAnnotation createForStatement(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public ParameterAnnotation createForParameter(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public MemberAnnotation deserializeForMember(CodeSerializationInput input, TypeContext context, IDefinitionMember member) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public DefinitionAnnotation deserializeForDefinition(CodeSerializationInput input, TypeContext context) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public StatementAnnotation deserializeForStatement(CodeSerializationInput input, StatementContext context) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public ParameterAnnotation deserializeForParameter(CodeSerializationInput input, TypeContext context) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}
}
