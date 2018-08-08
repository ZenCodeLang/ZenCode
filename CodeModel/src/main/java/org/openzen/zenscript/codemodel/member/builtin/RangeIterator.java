/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.builtin;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.iterator.ForeachIteratorVisitor;
import org.openzen.zenscript.codemodel.member.IIteratorMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.IteratorMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

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
	public BuiltinID getBuiltin() {
		return null;
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
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		type.addIterator(new IteratorMemberRef(this, mapper == null ? loopVariableTypes : mapper.map(loopVariableTypes)), priority);
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		throw new UnsupportedOperationException("Not a compilable member");
	}

	@Override
	public <T> T acceptForIterator(ForeachIteratorVisitor<T> visitor) {
		return visitor.visitIntRange();
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}

	@Override
	public void normalize(TypeScope scope) {
		
	}
}
