package org.openzen.zenscript.codemodel.partial;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

import java.util.Collections;
import java.util.List;

public class PartialDynamicMemberExpression implements IPartialExpression {
	private final CodePosition position;
	private final Expression value;
	private final TypeMembers typeMembers;
	private final String member;
	private final TypeScope scope;

	public PartialDynamicMemberExpression(CodePosition position, Expression value, TypeMembers typeMembers, String member, TypeScope scope) {
		this.position = position;
		this.value = value;
		this.typeMembers = typeMembers;
		this.member = member;
		this.scope = scope;
	}

	@Override
	public Expression eval() throws CompileException {
		if (typeMembers.hasOperator(OperatorType.MEMBERGETTER)) {
			return typeMembers.getOrCreateGroup(OperatorType.MEMBERGETTER).call(
					position,
					scope,
					value,
					new CallArguments(new ConstantStringExpression(position, member)),
					false);
		} else {
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Member not found: " + this.member);
		}
	}

	@Override
	public List<TypeID>[] predictCallTypes(CodePosition position, TypeScope scope, List<TypeID> hints, int arguments) throws CompileException {
		return new List[0];
	}

	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<TypeID> hints, int arguments) throws CompileException {
		return Collections.emptyList();
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<TypeID> hints, GenericName name) throws CompileException {
		return eval().getMember(position, scope, hints, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<TypeID> hints, CallArguments arguments) throws CompileException {
		return eval().call(position, scope, hints, arguments);
	}

	@Override
	public TypeID[] getTypeArguments() {
		return new TypeID[0];
	}

	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) throws CompileException {
		if (typeMembers.hasOperator(OperatorType.MEMBERSETTER)) {
			return typeMembers.getOrCreateGroup(OperatorType.MEMBERSETTER).call(
					position,
					scope,
					value,
					new CallArguments(
							new ConstantStringExpression(position, member),
							value
					),
					false);
		} else {
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Member not found: " + this.member);
		}
	}
}
