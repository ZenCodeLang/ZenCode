package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class ParsedExpressionOpAssign extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;
	private final OperatorType operator;

	public ParsedExpressionOpAssign(CodePosition position, ParsedExpression left, ParsedExpression right, OperatorType operator) {
		super(position);

		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		Expression cLeft = left.compile(scope).eval();
		TypeMembers typeMembers = scope.getTypeMembers(cLeft.type);
		TypeMemberGroup members = typeMembers.getOrCreateGroup(operator);
		if (members.getMethodMembers().isEmpty()) {
			members = typeMembers.getOrCreateGroup(operator.assignOperatorFor);
			Expression cRight = right.compile(scope.withHints(members.predictCallTypes(position, scope, scope.hints, 1)[0])).eval();
			Expression value = members.call(position, scope, cLeft, new CallArguments(cRight), false);
			return cLeft.assign(position, scope, value);
		} else {
			Expression cRight = right.compile(scope.withHints(members.predictCallTypes(position, scope, scope.hints, 1)[0])).eval();
			return members.call(position, scope, cLeft, new CallArguments(cRight), false);
		}
	}

	@Override
	public boolean hasStrongType() {
		return right.hasStrongType();
	}
}
