/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.ConcatMap;

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionExpression extends Expression {
	public final FunctionHeader header;
	public final LambdaClosure closure;
	public final Statement body;
	
	public FunctionExpression(
			CodePosition position,
			FunctionTypeID type,
			LambdaClosure closure,
			Statement body) {
		super(position, type, body.thrownType);
		
		this.header = type.header;
		this.closure = closure;
		this.body = body;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitFunction(this);
	}

	@Override
	public FunctionExpression transform(ExpressionTransformer transformer) {
		Statement tBody = body.transform(transformer, ConcatMap.empty());
		return tBody == body ? this : new FunctionExpression(position, (FunctionTypeID)type, closure, tBody);
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
			return ((ReturnStatement)body).value.transform(new ReturnExpressionTransformer(closure, filledArguments));
		} else {
			return null;
		}
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
				GetFunctionParameterExpression getParameter = (GetFunctionParameterExpression)original;
				if (filledArguments.containsKey(getParameter.parameter)) {
					return filledArguments.get(getParameter.parameter);
				}
			}
			
			return original;
		}
	}
}
