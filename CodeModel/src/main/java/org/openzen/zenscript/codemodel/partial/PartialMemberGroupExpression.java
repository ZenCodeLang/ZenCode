/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialMemberGroupExpression implements IPartialExpression {
	private final CodePosition position;
	private final Expression target;
	private final DefinitionMemberGroup group;
	private final boolean allowStaticUsage;
	
	public PartialMemberGroupExpression(CodePosition position, Expression target, DefinitionMemberGroup group, boolean allowStaticMembers) {
		this.position = position;
		this.target = target;
		this.group = group;
		this.allowStaticUsage = allowStaticMembers;
	}

	@Override
	public Expression eval() {
		return group.getter(position, target, allowStaticUsage);
	}
	
	@Override
	public List<ITypeID> getAssignHints() {
		if (group.getGetter() != null)
			return Collections.singletonList(group.getGetter().getType());
		if (group.getField() != null)
			return Collections.singletonList(group.getField().type);
		
		return Collections.emptyList();
	}

	@Override
	public List<ITypeID>[] predictCallTypes(TypeScope scope, List<ITypeID> hints, int arguments) {
		return group.predictCallTypes(scope, hints, arguments);
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<ITypeID> hints, int arguments) {
		List<FunctionHeader> results = group.getMethodMembers().stream()
				.filter(method -> method.member.getHeader().parameters.length == arguments && !method.member.isStatic())
				.map(method -> method.member.getHeader())
				.collect(Collectors.toList());
		if (results.isEmpty())
			System.out.println("!");
		return results;
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<ITypeID> hints, GenericName name) {
		return eval().getMember(position, scope, hints, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments) {
		return group.call(position, scope, target, arguments, allowStaticUsage);
	}
	
	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) {
		return group.setter(position, scope, target, value, allowStaticUsage);
	}
	
	@Override
	public IPartialExpression capture(CodePosition position, LambdaClosure closure) {
		return new PartialMemberGroupExpression(position, target.capture(position, closure).eval(), group, allowStaticUsage);
	}
}
