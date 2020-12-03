package org.openzen.zenscript.codemodel.partial;

import java.util.List;
import java.util.stream.Collectors;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;

public class PartialTypeExpression implements IPartialExpression {
	private final CodePosition position;
	private final TypeID type;
	private final TypeID[] typeArguments;
	
	public PartialTypeExpression(CodePosition position, TypeID type, TypeID[] typeArguments) {
		this.position = position;
		this.type = type;
		this.typeArguments = typeArguments;
	}

	@Override
	public Expression eval() {
		return new InvalidExpression(position, type, CompileExceptionCode.USING_TYPE_AS_EXPRESSION, "Not a valid expression");
	}

	@Override
	public List<TypeID>[] predictCallTypes(CodePosition position, TypeScope scope, List<TypeID> hints, int arguments) {
		TypeMemberGroup group = scope.getTypeMembers(type).getOrCreateGroup(OperatorType.CALL);
		if (group == null)
			return new List[0];

		return group.predictCallTypes(position, scope, hints, arguments);
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<TypeID> hints, int arguments) {
		TypeMemberGroup group = scope.getTypeMembers(type).getGroup(OperatorType.CALL);
		return group
				.getMethodMembers().stream()
				.filter(method -> method.member.getHeader().accepts(arguments) && method.member.isStatic())
				.map(method -> method.member.getHeader())
				.collect(Collectors.toList());
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<TypeID> hints, GenericName name) {
		return scope.getTypeMembers(type).getStaticMemberExpression(position, scope, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<TypeID> hints, CallArguments arguments) throws CompileException {
		if (arguments.getNumberOfTypeArguments() == 0 && (typeArguments != null && typeArguments.length > 0))
			arguments = new CallArguments(typeArguments, arguments.arguments);

		TypeMemberGroup group = scope.getTypeMembers(type).getOrCreateGroup(OperatorType.CALL);
		if (group == null)
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "This type has not call operator");

		return group.callStatic(position, type, scope, arguments);
	}

	@Override
	public TypeID[] getTypeArguments() {
		return typeArguments;
	}
	
	@Override
	public IPartialExpression capture(CodePosition position, LambdaClosure closure) {
		return this;
	}
}
