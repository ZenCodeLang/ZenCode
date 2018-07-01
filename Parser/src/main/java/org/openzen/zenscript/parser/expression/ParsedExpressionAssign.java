/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionAssign extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;

	public ParsedExpressionAssign(CodePosition position, ParsedExpression left, ParsedExpression right) {
		super(position);

		this.left = left;
		this.right = right;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		IPartialExpression cLeft = left.compile(scope);
		List<ITypeID> resultHints = cLeft.getAssignHints();
		
		Expression cRight = right.compile(scope.withHints(resultHints)).eval();
		return cLeft.assign(position, scope, cRight);
	}

	@Override
	public boolean hasStrongType() {
		return right.hasStrongType();
	}
}
