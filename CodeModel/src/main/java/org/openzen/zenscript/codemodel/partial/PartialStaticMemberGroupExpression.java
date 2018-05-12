/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.List;
import java.util.stream.Collectors;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.ICallableMember;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialStaticMemberGroupExpression implements IPartialExpression {
	public static PartialStaticMemberGroupExpression forMethod(CodePosition position, ITypeID target, ICallableMember method) {
		DefinitionMemberGroup group = new DefinitionMemberGroup(true);
		group.addMethod(method, TypeMemberPriority.SPECIFIED);
		return new PartialStaticMemberGroupExpression(position, target, group);
	}
	
	private final CodePosition position;
	private final ITypeID target;
	private final DefinitionMemberGroup group;
	
	public PartialStaticMemberGroupExpression(CodePosition position, ITypeID target, DefinitionMemberGroup group) {
		this.position = position;
		this.group = group;
		this.target = target;
	}
	
	@Override
	public Expression eval() {
		return group.staticGetter(position);
	}

	@Override
	public List<ITypeID>[] predictCallTypes(TypeScope scope, List<ITypeID> hints, int arguments) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<ITypeID> hints, int arguments) {
		return group.getMethodMembers().stream()
				.filter(method -> method.member.getHeader().parameters.length == arguments && method.member.isStatic())
				.map(method -> method.member.getHeader())
				.collect(Collectors.toList());
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<ITypeID> hints, GenericName name) {
		return eval().getMember(position, scope, hints, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments) {
		return group.callStatic(position, target, scope, arguments);
	}
	
	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) {
		return group.staticSetter(position, scope, value);
	}
}
