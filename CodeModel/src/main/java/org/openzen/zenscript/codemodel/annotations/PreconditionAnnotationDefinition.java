package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.FunctionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationInput;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PreconditionAnnotationDefinition implements AnnotationDefinition {
	public static final PreconditionAnnotationDefinition INSTANCE = new PreconditionAnnotationDefinition();

	private final List<GenericName> enforcementLevelName = Arrays.asList(
			new GenericName("stdlib"),
			new GenericName("EnforcementLevel"));

	private PreconditionAnnotationDefinition() {
	}

	@Override
	public String getAnnotationName() {
		return "Precondition";
	}

	@Override
	public List<FunctionHeader> getInitializers(BaseScope scope) {
		return Collections.singletonList(new FunctionHeader(
				BasicTypeID.VOID,
				scope.getType(CodePosition.BUILTIN, enforcementLevelName),
				BasicTypeID.BOOL,
				BasicTypeID.STRING));
	}

	@Override
	public ExpressionScope getScopeForMember(IDefinitionMember member, BaseScope scope) {
		if (member instanceof FunctionalMember) {
			FunctionHeader header = ((FunctionalMember) member).header;
			return new ExpressionScope(new FunctionScope(((FunctionalMember) member).position, scope, header));
		} else {
			throw new UnsupportedOperationException("Can only assign preconditions to methods");
		}
	}

	@Override
	public ExpressionScope getScopeForType(HighLevelDefinition definition, BaseScope scope) {
		if (definition instanceof FunctionDefinition) {
			FunctionHeader header = ((FunctionDefinition) definition).header;
			return new ExpressionScope(new FunctionScope(((FunctionDefinition) definition).position, scope, header));
		} else {
			throw new UnsupportedOperationException("Can only assign preconditions to functions");
		}
	}

	@Override
	public ExpressionScope getScopeForStatement(Statement statement, StatementScope scope) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public ExpressionScope getScopeForParameter(FunctionHeader header, FunctionParameter parameter, BaseScope scope) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public MemberAnnotation createForMember(CodePosition position, CallArguments arguments) {
		String enforcement = arguments.arguments[0].evaluate().flatMap(CompileTimeConstant::asEnumValue)
				.map(c -> c.member.name)
				.orElse("INVALID");
		Expression condition = arguments.arguments[1];
		Expression message = arguments.arguments[2];
		return new PreconditionForMethod(position, enforcement, condition, message);
	}

	@Override
	public DefinitionAnnotation createForDefinition(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public StatementAnnotation createForStatement(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public ParameterAnnotation createForParameter(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public MemberAnnotation deserializeForMember(CodeSerializationInput input, TypeContext context, IDefinitionMember member) {
		CodePosition position = input.deserializePosition();
		String enforcement = input.readString();
		StatementContext statementContext = new StatementContext(position, context, member.getHeader());
		Expression condition = input.deserializeExpression(statementContext);
		Expression message = input.deserializeExpression(statementContext);
		return new PreconditionForMethod(position, enforcement, condition, message);
	}

	@Override
	public DefinitionAnnotation deserializeForDefinition(CodeSerializationInput input, TypeContext context) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public StatementAnnotation deserializeForStatement(CodeSerializationInput input, StatementContext context) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ParameterAnnotation deserializeForParameter(CodeSerializationInput input, TypeContext context) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
