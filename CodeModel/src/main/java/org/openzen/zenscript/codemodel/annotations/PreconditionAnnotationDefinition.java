package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationInput;
import org.openzen.zenscript.codemodel.serialization.StatementSerializationContext;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

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
	public List<AnnotationInitializer> getInitializers(TypeBuilder types) {
		TypeID enforcementLevel = types.resolve(CodePosition.BUILTIN, enforcementLevelName)
				.orElseThrow(() -> new RuntimeException("Could not find stdlib.EnforcementLevel"));

		return Collections.singletonList(new AnnotationInitializer(new FunctionHeader(
				BasicTypeID.VOID,
				enforcementLevel,
				BasicTypeID.BOOL,
				BasicTypeID.STRING)));
	}

	@Override
	public ExpressionCompiler getScopeForMember(IDefinitionMember member, MemberCompiler compiler) {
		if (member instanceof FunctionalMember) {
			FunctionHeader header = ((FunctionalMember) member).header;
			return compiler.forMethod(header).expressions();
		} else {
			throw new UnsupportedOperationException("Can only assign preconditions to methods");
		}
	}

	@Override
	public ExpressionCompiler getScopeForType(HighLevelDefinition definition, DefinitionCompiler compiler) {
		if (definition instanceof FunctionDefinition) {
			FunctionHeader header = ((FunctionDefinition) definition).header;
			return compiler.forMembers(definition).forMethod(header).expressions();
		} else {
			throw new UnsupportedOperationException("Can only assign preconditions to functions");
		}
	}

	@Override
	public ExpressionCompiler getScopeForStatement(Statement statement, StatementCompiler compiler) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public ExpressionCompiler getScopeForParameter(FunctionHeader header, FunctionParameter parameter, ExpressionCompiler compiler) {
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
	public MemberAnnotation deserializeForMember(CodeSerializationInput input, TypeSerializationContext context, IDefinitionMember member) {
/*		CodePosition position = input.deserializePosition();
		String enforcement = input.readString();
		StatementContext statementContext = new StatementContext(position, context, member.getHeader());
		Expression condition = input.deserializeExpression(statementContext);
		Expression message = input.deserializeExpression(statementContext);
		return new PreconditionForMethod(position, enforcement, condition, message);*/
		throw new UnsupportedOperationException("Not yet supported");
	}

	@Override
	public DefinitionAnnotation deserializeForDefinition(CodeSerializationInput input, TypeSerializationContext context) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public StatementAnnotation deserializeForStatement(CodeSerializationInput input, StatementSerializationContext context) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ParameterAnnotation deserializeForParameter(CodeSerializationInput input, TypeSerializationContext context) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
