package org.openzen.zenscript.codemodel.partial;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;

import java.util.List;

public class PartialSuperExpression implements IPartialExpression {
	private final CodePosition position;
	private final TypeID superType;

	public PartialSuperExpression(CodePosition position, TypeID superType) {
		this.position = position;
		this.superType = superType;
	}

	@Override
	public Expression eval() throws CompileException {
		return new InvalidExpression(position, BasicTypeID.UNDETERMINED, CompileExceptionCode.VARIANT_OPTION_NOT_AN_EXPRESSION, "Cannot use a variant option as expression");
	}

	@Override
	public List<TypeID>[] predictCallTypes(CodePosition position, TypeScope scope, List<TypeID> hints, int arguments) throws CompileException {
		TypeMemberGroup group = scope.getTypeMembers(superType).getOrCreateGroup(OperatorType.CONSTRUCTOR);
		if (group == null)
			return new List[0];

		return group.predictCallTypes(position, scope, hints, arguments);
	}

	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<TypeID> hints, int arguments) throws CompileException {
		return null;
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<TypeID> hints, GenericName name) throws CompileException {
		if (name.hasArguments())
			throw new CompileException(position, CompileExceptionCode.INVALID_TYPE_ARGUMENTS, "Type arguments not allowed here");

		return scope.getTypeMembers(superType)
				.getGroup(name.name)
				.map(group -> new PartialStaticMemberGroupExpression(position, scope, superType, group, name.arguments))
				.orElseThrow(() -> new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "No member " + name.name + " on super type"));
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<TypeID> hints, CallArguments arguments) throws CompileException {
		return null;
	}

	@Override
	public TypeID[] getTypeArguments() {
		return new TypeID[0];
	}
}
