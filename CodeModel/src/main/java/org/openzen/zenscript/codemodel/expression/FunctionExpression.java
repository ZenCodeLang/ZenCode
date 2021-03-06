package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FunctionExpression extends Expression {
	public final FunctionHeader header;
	public final LambdaClosure closure;
	public final Statement body;

	public FunctionExpression(
			CodePosition position,
			TypeID type,
			LambdaClosure closure,
			FunctionHeader header,
			Statement body) {
		super(position, type, body.thrownType);

		this.header = header;
		this.closure = closure;
		this.body = body;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitFunction(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitFunction(context, this);
	}

	@Override
	public FunctionExpression transform(ExpressionTransformer transformer) {
		Statement tBody = body.transform(transformer, ConcatMap.empty(LoopStatement.class, LoopStatement.class));
		return tBody == body ? this : new FunctionExpression(position, type, closure, header, tBody);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		body.forEachStatement(consumer);
	}

	/**
	 * Checks if this is a simple function expression. A simple function
	 * expression consists of a body with just a expression or return statement.
	 *
	 * @return
	 */
	public boolean isSimple() {
		return body instanceof ReturnStatement || body instanceof ExpressionStatement;
	}

	public Expression asReturnExpression(Expression... arguments) {
		Map<FunctionParameter, Expression> filledArguments = new HashMap<>();
		if (body instanceof ReturnStatement) {
			return ((ReturnStatement) body).value.transform(new ReturnExpressionTransformer(closure, filledArguments));
		} else {
			return null;
		}
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new FunctionExpression(position, type, closure, header, body.normalize(scope, ConcatMap.empty(LoopStatement.class, LoopStatement.class)));
	}

	private static class ReturnExpressionTransformer implements ExpressionTransformer {
		private final LambdaClosure closure;
		private final Map<FunctionParameter, Expression> filledArguments;

		public ReturnExpressionTransformer(LambdaClosure closure, Map<FunctionParameter, Expression> filledArguments) {
			this.closure = closure;
			this.filledArguments = filledArguments;
		}

		@Override
		public Expression transform(Expression original) {
			if (original instanceof GetFunctionParameterExpression) {
				GetFunctionParameterExpression getParameter = (GetFunctionParameterExpression) original;
				if (filledArguments.containsKey(getParameter.parameter)) {
					return filledArguments.get(getParameter.parameter);
				}
			}

			return original;
		}
	}
}
