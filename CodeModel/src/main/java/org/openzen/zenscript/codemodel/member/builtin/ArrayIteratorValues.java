/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.builtin;

import java.util.Map;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.iterator.ForeachIteratorVisitor;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.IIteratorMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ArrayIteratorValues implements IIteratorMember {
	private final ArrayTypeID type;
	private final ITypeID[] loopVariableTypes;
	
	public ArrayIteratorValues(ArrayTypeID type) {
		this.type = type;
		loopVariableTypes = new ITypeID[] { type.elementType };
	}
	
	@Override
	public CodePosition getPosition() {
		return CodePosition.BUILTIN;
	}

	@Override
	public int getLoopVariableCount() {
		return 1;
	}

	@Override
	public ITypeID[] getLoopVariableTypes() {
		return loopVariableTypes;
	}

	@Override
	public String describe() {
		return "iterator for array values";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addIterator(this, priority);
	}

	@Override
	public IDefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return new ArrayIteratorValues(type.withGenericArguments(registry, mapping));
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		throw new UnsupportedOperationException("Not a compilable member");
	}

	@Override
	public <T> T acceptForIterator(ForeachIteratorVisitor<T> visitor) {
		return visitor.visitArrayValueIterator();
	}
}
