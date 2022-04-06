package org.openzen.zenscript.codemodel.partial;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;

import java.util.List;
import java.util.stream.Collectors;

public class PartialStaticMemberGroupExpression implements IPartialExpression {
	private final CodePosition position;
	private final TypeScope scope;
	private final TypeID target;
	private final TypeMemberGroup group;
	private final TypeID[] typeArguments;

	public PartialStaticMemberGroupExpression(CodePosition position, TypeScope scope, TypeID target, TypeMemberGroup group, TypeID[] typeArguments) {
		this.position = position;
		this.scope = scope;
		this.group = group;
		this.target = target;
		this.typeArguments = typeArguments;
	}

	@Override
	public Expression eval() throws CompileException {
		return group.staticGetter(position, scope);
	}

	@Override
	public List<TypeID>[] predictCallTypes(CodePosition position, TypeScope scope, List<TypeID> hints, int arguments) {
		return group.predictCallTypes(position, scope, hints, arguments);
	}

	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<TypeID> hints, int arguments) {
		return group.getMethodMembers().stream()
				.filter(method -> method.member.getHeader().accepts(arguments) && method.member.isStatic())
				.map(method -> method.member.getHeader())
				.collect(Collectors.toList());
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<TypeID> hints, GenericName name) throws CompileException {
		return eval().getMember(position, scope, hints, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<TypeID> hints, CallArguments arguments) throws CompileException {
		return group.callStatic(position, target, scope, arguments);
	}

	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) throws CompileException {
		return group.staticSetter(position, scope, value);
	}

	@Override
	public TypeID[] getTypeArguments() {
		return typeArguments;
	}

	@Override
	public List<TypeID> getAssignHints() {
		return group.getAssignHints();
	}
}
