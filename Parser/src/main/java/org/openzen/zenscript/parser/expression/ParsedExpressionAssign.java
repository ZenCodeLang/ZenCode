package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidAssignExpression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

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
			List<TypeID> resultHints = cLeft.getAssignHints();
			Expression cRight = right.compile(scope.withHints(resultHints)).eval();
			return cLeft.assign(position, scope, cRight);
		} catch (CompileException ex) {
			InvalidExpression invalid = new InvalidExpression(BasicTypeID.VOID, ex);
			Expression cRight;
			try {
				cRight = right.compile(scope).eval();
			} catch (CompileException ex2) {
				cRight = new InvalidExpression(BasicTypeID.VOID, ex2);
			}
			return new InvalidAssignExpression(position, invalid, cRight);
		}
	}

	@Override
	public boolean hasStrongType() {
		return right.hasStrongType();
	}
}
