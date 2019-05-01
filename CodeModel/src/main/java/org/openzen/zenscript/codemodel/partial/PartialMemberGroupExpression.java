/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.member.TypeMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialMemberGroupExpression implements IPartialExpression {
	private final CodePosition position;
	private final Expression target;
	private final TypeMemberGroup group;
	private final StoredType[] typeArguments;
	private final boolean allowStaticUsage;
	private final TypeScope scope;
	
	public PartialMemberGroupExpression(
			CodePosition position,
			TypeScope scope,
			Expression target,
			TypeMemberGroup group,
			StoredType[] typeArguments,
			boolean allowStaticMembers)
	{
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
			StoredType[] typeArguments,
			boolean allowStaticMembers)
	{
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
	public List<StoredType> getAssignHints() {
		if (group.getSetter() != null)
			return Collections.singletonList(group.getSetter().getType());
		if (group.getField() != null)
			return Collections.singletonList(group.getField().getType());
		
		return Collections.emptyList();
	}

	@Override
	public List<StoredType>[] predictCallTypes(CodePosition position, TypeScope scope, List<StoredType> hints, int arguments) {
		return group.predictCallTypes(position, scope, hints, arguments);
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<StoredType> hints, int arguments) {
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
		if (results.isEmpty())
			System.out.println("!");
		return results;
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<StoredType> hints, GenericName name) throws CompileException {
		return eval().getMember(position, scope, hints, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<StoredType> hints, CallArguments arguments) throws CompileException {
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
	public StoredType[] getTypeArguments() {
		return typeArguments;
	}
}
