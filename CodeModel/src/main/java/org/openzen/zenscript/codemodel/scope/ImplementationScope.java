/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import java.util.List;
import java.util.function.Function;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ImplementationScope extends BaseScope {
	private final BaseScope outer;
	private final ImplementationMember implementation;
	private final TypeMembers members;
	
	public ImplementationScope(BaseScope outer, ImplementationMember implementation) {
		this.outer = outer;
		this.implementation = implementation;
		
		TypeMembers interfaceMembers = outer.getMemberCache().get(implementation.type);
		members = new TypeMembers(outer.getMemberCache(), implementation.type);
		interfaceMembers.copyMembersTo(implementation.position, interfaceMembers, TypeMemberPriority.INHERITED);
		
		for (IDefinitionMember member : implementation.members) {
			member.registerTo(members, TypeMemberPriority.SPECIFIED);
		}
	}

	@Override
	public LocalMemberCache getMemberCache() {
		return outer.getMemberCache();
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) {
		if (members.hasInnerType(name.name))
			return new PartialTypeExpression(position, members.getInnerType(position, name), name.arguments);
		if (members.hasMember(name.name))
			return members.getMemberExpression(position, new ThisExpression(position, outer.getThisType()), name, true);
		
		return outer.get(position, name);
	}

	@Override
	public ITypeID getType(CodePosition position, List<GenericName> name) {
		if (members.hasInnerType(name.get(0).name)) {
			ITypeID result = members.getInnerType(position, name.get(0));
			for (int i = 1; i < name.size(); i++) {
				result = getTypeMembers(result).getInnerType(position, name.get(i));
			}
			return result;
		}
		
		return outer.getType(position, name);
	}

	@Override
	public LoopStatement getLoop(String name) {
		return null;
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return null;
	}

	@Override
	public ITypeID getThisType() {
		return outer.getThisType();
	}

	@Override
	public Function<CodePosition, Expression> getDollar() {
		return outer.getDollar();
	}

	@Override
	public IPartialExpression getOuterInstance(CodePosition position) {
		return new ThisExpression(position, outer.getThisType());
	}

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return outer.getAnnotation(name);
	}
}
