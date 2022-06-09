package org.openzen.zenscript.codemodel.partial;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMember;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PartialMemberGroupExpression implements IPartialExpression {
	private final CodePosition position;
	private final Expression target;
	private final TypeMemberGroup group;
	private final TypeID[] typeArguments;
	private final boolean allowStaticUsage;
	private final TypeScope scope;

	public PartialMemberGroupExpression(
			CodePosition position,
			TypeScope scope,
			Expression target,
			TypeMemberGroup group,
			TypeID[] typeArguments,
			boolean allowStaticMembers) {
		this.position = position;
		this.scope = scope;
		this.target = target;
		this.group = group;
		this.typeArguments = typeArguments;
		this.allowStaticUsage = allowStaticMembers;
	}

	public PartialMemberGroupExpression(
			CodePosition position,
			TypeScope scope,
			Expression target,
			String name,
			FunctionalMemberRef member,
			TypeID[] typeArguments,
			boolean allowStaticMembers) {
		this.position = position;
		this.scope = scope;
		this.target = target;
		this.group = TypeMemberGroup.forMethod(name, member);
		this.typeArguments = typeArguments;
		this.allowStaticUsage = allowStaticMembers;
	}

	@Override
	public Expression eval() throws CompileException {
		return group.getter(position, scope, target, allowStaticUsage);
	}

	@Override
	public List<TypeID> getAssignHints() {
		if (group.getSetter().isPresent())
			return Collections.singletonList(group.getSetter().get().getType());
		if (group.getField().isPresent())
			return Collections.singletonList(group.getField().get().getType());

		return Collections.emptyList();
	}

	@Override
	public List<TypeID>[] predictCallTypes(CodePosition position, TypeScope scope, List<TypeID> hints, int arguments) {
		return group.predictCallTypes(position, scope, hints, arguments);
	}

	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<TypeID> hints, int arguments) {
		List<FunctionHeader> results = new ArrayList<>();
		for (TypeMember<FunctionalMemberRef> method : group.getMethodMembers()) {
			if (!method.member.accepts(arguments) || method.member.isStatic())
				continue;

			try {
				scope.getPreparer().prepare(method.member.getTarget());
				results.add(method.member.getHeader());
			} catch (CompileException ex) {
				// ignore this here
			}
		}
		//if (results.isEmpty())
		//	System.out.println("!");
		return results;
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<TypeID> hints, GenericName name) throws CompileException {
		return eval().getMember(position, scope, hints, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<TypeID> hints, CallArguments arguments) throws CompileException {
		return group.call(position, scope, target, arguments, allowStaticUsage);
	}

	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) throws CompileException {
		return group.setter(position, scope, target, value, allowStaticUsage);
	}

	@Override
	public IPartialExpression capture(CodePosition position, LambdaClosure closure) throws CompileException {
		return new PartialMemberGroupExpression(position, scope, target.capture(position, closure).eval(), group, typeArguments, allowStaticUsage);
	}

	@Override
	public TypeID[] getTypeArguments() {
		return typeArguments;
	}
}
