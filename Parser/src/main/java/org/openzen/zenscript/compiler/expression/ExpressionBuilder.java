package org.openzen.zenscript.compiler.expression;

import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.BinaryExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.ResolvedCallable;
import org.openzen.zenscript.compiler.types.MemberGroup;

public interface ExpressionBuilder {

	Expression binary(BinaryExpression.Operator operator, Expression left, Expression right);

	Expression coalesce(Expression left, Expression right);

	Expression constant(boolean value);

	Expression constant(String value);

	Expression constant(float value);

	Expression constant(double value);

	Expression getThis(TypeID thisType);

	Expression instanceMemberCall(Expression instance, MemberGroup group, CompilingExpression... arguments);

	Expression instanceMemberGet(Expression instance, MemberGroup group);

	Expression instanceMemberSet(Expression instance, MemberGroup group, Expression value);

	Expression invalid(CompileExceptionCode code, String message);

	Expression invalidLValue();

	Expression ternary(Expression condition, Expression ifThen, Expression ifElse);

	Expression call(ResolvedCallable constructor, CompilingExpression... arguments);
}
