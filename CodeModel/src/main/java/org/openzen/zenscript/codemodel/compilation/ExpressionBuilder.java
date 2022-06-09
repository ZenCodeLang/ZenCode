package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CompileError;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

public interface ExpressionBuilder {
	Expression binary(BinaryExpression.Operator operator, Expression left, Expression right);

	Expression callStatic(MethodInstance method, CallArguments arguments);

	Expression callVirtual(MethodInstance method, Expression target, CallArguments arguments);

	Expression coalesce(Expression left, Expression right);

	Expression constant(boolean value);

	Expression constant(String value);

	Expression constant(float value);

	Expression constant(double value);

	Expression constantNull(TypeID type);

	Expression getThis(TypeID thisType);

	Expression getLocalVariable(VarStatement variable);

	Expression interfaceCast(ImplementationMember implementation, Expression value);

	Expression invalid(CompileError error);

	Expression invalid(CompileError error, TypeID type);

	Expression is(Expression value, TypeID type);

	Expression newArray(ArrayTypeID type, Expression[] values);

	Expression newAssoc(AssocTypeID type, List<Expression> keys, List<Expression> values);

	Expression newGenericMap(GenericMapTypeID type);

	Expression newRange(Expression from, Expression to);

	Expression match(Expression value, TypeID resultingType, MatchExpression.Case[] cases);

	Expression panic(TypeID type, Expression value);

	Expression setLocalVariable(VarStatement variable, Expression value);

	Expression ternary(Expression condition, Expression ifThen, Expression ifElse);

	Expression throw_(TypeID type, Expression value);

	Expression tryConvert(Expression value, TypeID resultingType);

	Expression tryRethrowAsException(Expression value, TypeID resultingType);

	Expression tryRethrowAsResult(Expression value, TypeID resultingType);

	Expression unary(UnaryExpression.Operator operator, Expression value);
}
