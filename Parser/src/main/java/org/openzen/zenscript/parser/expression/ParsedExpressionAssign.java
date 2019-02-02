/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidAssignExpression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;

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
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		try {
			IPartialExpression cLeft = left.compile(scope);
			List<StoredType> resultHints = cLeft.getAssignHints();
			Expression cRight = right.compile(scope.withHints(resultHints)).eval();
			return cLeft.assign(position, scope, cRight);
		} catch (CompileException ex) {
			InvalidExpression invalid = new InvalidExpression(BasicTypeID.VOID.stored, ex);
			Expression cRight;
			try {
				cRight = right.compile(scope).eval();
			} catch (CompileException ex2) {
				cRight = new InvalidExpression(BasicTypeID.VOID.stored, ex2);
			}
			return new InvalidAssignExpression(position, invalid, cRight);
		}
	}

	@Override
	public boolean hasStrongType() {
		return right.hasStrongType();
	}
}
