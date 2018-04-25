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
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.Taggable;

/**
 *
 * @author Hoofdgebruiker
 */
public class RangeIterator extends Taggable implements IIteratorMember {
	private final ITypeID[] loopVariableTypes;
	
	public RangeIterator(RangeTypeID type) {
		if (type.from != type.to)
			throw new UnsupportedOperationException("Cannot iterator over a range with different from and to types");
		if (type.from != BasicTypeID.BYTE
				&& type.from != BasicTypeID.SBYTE
				&& type.from != BasicTypeID.SHORT
				&& type.from != BasicTypeID.USHORT
				&& type.from != BasicTypeID.INT
				&& type.from != BasicTypeID.UINT
				&& type.from != BasicTypeID.LONG
				&& type.from != BasicTypeID.ULONG)
			throw new UnsupportedOperationException("Can only use range iterator over integer types");
		
		loopVariableTypes = new ITypeID[] { type.from };
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
		return "range iterator";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addIterator(this, priority);
	}

	@Override
	public IDefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return this; // only use for basic types
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		throw new UnsupportedOperationException("Not a compilable member");
	}

	@Override
	public <T> T acceptForIterator(ForeachIteratorVisitor<T> visitor) {
		return visitor.visitIntRange();
	}
}
