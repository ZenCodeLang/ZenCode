package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.UnaryExpression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;

public class UnaryVirtualOperatorMember implements ICallableMember {
	private final UnaryExpression.Operator operator;
	private final GlobalTypeRegistry typeRegistry;
	private final TypeID operandType;

	public UnaryVirtualOperatorMember(UnaryExpression.Operator operator, GlobalTypeRegistry typeRegistry, TypeID operandType) {
		this.operator = operator;
		this.typeRegistry = typeRegistry;
		this.operandType = operandType;
	}

	@Override
	public Expression callVirtual(CodePosition position, TypeScope scope, Expression target, CallArguments arguments) {
		return new UnaryExpression(position, target, operator, typeRegistry);
	}

	@Override
	public Expression callStatic(CodePosition position, TypeScope scope, CallArguments arguments) {
		throw new UnsupportedOperationException();
	}
}
