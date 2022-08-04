package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationInput;
import org.openzen.zenscript.codemodel.serialization.StatementSerializationContext;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;
import org.openzen.zenscript.codemodel.statement.Statement;

import java.util.Collections;
import java.util.List;

public class InvalidAnnotationDefinition implements AnnotationDefinition {
	public static final InvalidAnnotationDefinition INSTANCE = new InvalidAnnotationDefinition();

	private InvalidAnnotationDefinition() {
	}

	@Override
	public String getAnnotationName() {
		return "Invalid";
	}

	@Override
	public List<AnnotationInitializer> getInitializers(TypeBuilder types) {
		return Collections.emptyList();
	}

	@Override
	public ExpressionCompiler getScopeForMember(IDefinitionMember member, MemberCompiler compiler) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public ExpressionCompiler getScopeForType(HighLevelDefinition definition, DefinitionCompiler compiler) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public ExpressionCompiler getScopeForStatement(Statement statement, StatementCompiler compiler) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public ExpressionCompiler getScopeForParameter(FunctionHeader header, FunctionParameter parameter, ExpressionCompiler compiler) {
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
	public MemberAnnotation deserializeForMember(CodeSerializationInput input, TypeSerializationContext context, IDefinitionMember member) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public DefinitionAnnotation deserializeForDefinition(CodeSerializationInput input, TypeSerializationContext context) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public StatementAnnotation deserializeForStatement(CodeSerializationInput input, StatementSerializationContext context) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}

	@Override
	public ParameterAnnotation deserializeForParameter(CodeSerializationInput input, TypeSerializationContext context) {
		throw new UnsupportedOperationException("Not a valid annotation");
	}
}
