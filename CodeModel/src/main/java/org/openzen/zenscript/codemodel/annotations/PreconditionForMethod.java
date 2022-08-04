package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.PanicExpression;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.StatementSerializationContext;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.IfStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreconditionForMethod implements MemberAnnotation {
	private final CodePosition position;
	private final String enforcement;
	private final Expression condition;
	private final Expression message;

	public PreconditionForMethod(
			CodePosition position,
			String enforcement,
			Expression condition,
			Expression message) {
		this.position = position;
		this.enforcement = enforcement;
		this.condition = condition;
		this.message = message;
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return PreconditionAnnotationDefinition.INSTANCE;
	}

	@Override
	public void apply(IDefinitionMember member) {
		if (member instanceof GetterMember) {
			applyOnOverridingGetter((GetterMember) member);
		} else if (member instanceof SetterMember) {
			applyOnOverridingSetter((SetterMember) member);
		} else if (member instanceof FunctionalMember) {
			applyOnOverridingMethod((FunctionalMember) member);
		}
	}

	@Override
	public void applyOnOverridingMethod(FunctionalMember member) {
		member.body = applyOnOverride(member.body);
	}

	@Override
	public void applyOnOverridingGetter(GetterMember member) {
		member.body = applyOnOverride(member.body);
	}

	@Override
	public void applyOnOverridingSetter(SetterMember member) {
		member.body = applyOnOverride(member.body);
	}

	private Statement applyOnOverride(Statement body) {
		if (body == null)
			return body;

		Expression inverseCondition = new CallExpression(
				position,
				condition,
				new MethodInstance(BuiltinMethodSymbol.BOOL_NOT),
				CallArguments.EMPTY);

		Statement throwStatement = new ExpressionStatement(position, new PanicExpression(position, BasicTypeID.VOID, message));
		List<Statement> statements = new ArrayList<>();
		statements.add(new IfStatement(position, inverseCondition, throwStatement, null));

		if (body instanceof BlockStatement) {
			statements.addAll(Arrays.asList(((BlockStatement) body).statements));
		} else {
			statements.add(body);
		}
		return new BlockStatement(position, statements.toArray(new Statement[0]));
	}

	@Override
	public void serialize(CodeSerializationOutput output, IDefinitionMember member, TypeSerializationContext context) {
		output.serialize(position);
		output.writeString(enforcement);
		StatementSerializationContext statementContext = context.forMethod(member.getHeader());
		output.serialize(statementContext, condition);
		output.serialize(statementContext, message);
	}
}
