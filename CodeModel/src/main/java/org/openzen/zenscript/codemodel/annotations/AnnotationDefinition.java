package org.openzen.zenscript.codemodel.annotations;

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

import java.util.List;

public interface AnnotationDefinition {
	String getAnnotationName();

	List<FunctionHeader> getInitializers(BaseScope scope);

	ExpressionScope getScopeForMember(IDefinitionMember member, BaseScope scope);

	ExpressionScope getScopeForType(HighLevelDefinition definition, BaseScope scope);

	ExpressionScope getScopeForStatement(Statement statement, StatementScope scope);

	ExpressionScope getScopeForParameter(FunctionHeader header, FunctionParameter parameter, BaseScope scope);

	MemberAnnotation createForMember(CodePosition position, CallArguments arguments);

	DefinitionAnnotation createForDefinition(CodePosition position, CallArguments arguments);

	StatementAnnotation createForStatement(CodePosition position, CallArguments arguments);

	ParameterAnnotation createForParameter(CodePosition position, CallArguments arguments);

	MemberAnnotation deserializeForMember(CodeSerializationInput input, TypeContext context, IDefinitionMember member);

	DefinitionAnnotation deserializeForDefinition(CodeSerializationInput input, TypeContext context);

	StatementAnnotation deserializeForStatement(CodeSerializationInput input, StatementContext context);

	ParameterAnnotation deserializeForParameter(CodeSerializationInput input, TypeContext context);
}
