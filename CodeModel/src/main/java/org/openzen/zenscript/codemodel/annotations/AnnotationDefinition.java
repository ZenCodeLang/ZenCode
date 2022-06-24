package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationInput;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;
import org.openzen.zenscript.codemodel.statement.Statement;

public interface AnnotationDefinition {
	String getAnnotationName();

	StaticCallable getInitializers();

	ExpressionCompiler getScopeForMember(IDefinitionMember member, MemberCompiler compiler);

	ExpressionCompiler getScopeForType(HighLevelDefinition definition, DefinitionCompiler compiler);

	ExpressionCompiler getScopeForStatement(Statement statement, StatementCompiler compiler);

	ExpressionCompiler getScopeForParameter(FunctionHeader header, FunctionParameter parameter, ExpressionCompiler compiler);

	MemberAnnotation createForMember(CodePosition position, CallArguments arguments);

	DefinitionAnnotation createForDefinition(CodePosition position, CallArguments arguments);

	StatementAnnotation createForStatement(CodePosition position, CallArguments arguments);

	ParameterAnnotation createForParameter(CodePosition position, CallArguments arguments);

	MemberAnnotation deserializeForMember(CodeSerializationInput input, TypeSerializationContext context, IDefinitionMember member);

	DefinitionAnnotation deserializeForDefinition(CodeSerializationInput input, TypeSerializationContext context);

	StatementAnnotation deserializeForStatement(CodeSerializationInput input, StatementContext context);

	ParameterAnnotation deserializeForParameter(CodeSerializationInput input, TypeSerializationContext context);
}
