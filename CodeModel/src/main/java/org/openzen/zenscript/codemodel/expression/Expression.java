package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.compilation.expression.WrappedCompilingExpression;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.StatementTransformer;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class Expression {
	public static final Expression[] NONE = new Expression[0];

	public final CodePosition position;
	public final TypeID type;
	public final TypeID thrownType;

	public Expression(CodePosition position, TypeID type, TypeID thrownType) {
		if (type == null)
			throw new NullPointerException();
		//if (type.type == BasicTypeID.UNDETERMINED)
		//	throw new IllegalArgumentException(position + ": Cannot use undetermined type as expression type");

		this.position = position;
		this.type = type;
		this.thrownType = thrownType;
	}

	public static TypeID binaryThrow(CodePosition position, TypeID left, TypeID right) {
		if (left == right)
			return left;
		else if (left == null)
			return right;
		else if (right == null)
			return left;
		else
			return new InvalidTypeID(position, new CompileError(CompileExceptionCode.DIFFERENT_EXCEPTIONS, "two different exceptions in same operation: " + left + " and " + right));
	}

	public static TypeID multiThrow(CodePosition position, Expression[] expressions) {
		TypeID result = null;
		for (Expression expression : expressions)
			result = binaryThrow(position, result, expression.thrownType);
		return result;
	}

	public static Expression[] transform(Expression[] expressions, ExpressionTransformer transformer) {
		Expression[] tExpressions = new Expression[expressions.length];
		boolean changed = false;
		for (int i = 0; i < tExpressions.length; i++) {
			Expression tExpression = expressions[i].transform(transformer);
			changed |= tExpression != expressions[i];
			tExpressions[i] = tExpression;
		}
		return changed ? tExpressions : expressions;
	}

	public abstract <T> T accept(ExpressionVisitor<T> visitor);

	public abstract <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor);

	public abstract Expression transform(ExpressionTransformer transformer);

	public final Expression transform(StatementTransformer transformer) {
		return transform((ExpressionTransformer) expression -> {
			if (expression instanceof FunctionExpression) {
				FunctionExpression function = (FunctionExpression) expression;
				Statement body = function.body.transform(transformer);
				if (body == function.body)
					return function;

				return new FunctionExpression(function.position, function.closure, function.header, body);
			} else {
				return expression;
			}
		});
	}

	/**
	 * Determines if this expression aborts execution; that is, it is either a
	 * throw or a panic expression.
	 *
	 * @return abort flag
	 */
	public boolean aborts() {
		return false;
	}

	public void forEachStatement(Consumer<Statement> consumer) {

	}

	public Optional<CompileTimeConstant> evaluate() {
		return Optional.empty();
	}

	public final CompilingExpression wrap(ExpressionCompiler compiler) {
		return new WrappedCompilingExpression(compiler, this);
	}
}
