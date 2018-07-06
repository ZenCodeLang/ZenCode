/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.builtin;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.iterator.ForeachIteratorVisitor;
import org.openzen.zenscript.codemodel.member.IIteratorMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.member.ref.IteratorMemberRef;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.Taggable;

/**
 *
 * @author Hoofdgebruiker
 */
public class StringCharIterator extends Taggable implements IIteratorMember {
	private static final ITypeID[] TYPES = new ITypeID[] { BasicTypeID.CHAR };

	@Override
	public int getLoopVariableCount() {
		return 1;
	}
	
	@Override
	public BuiltinID getBuiltin() {
		return null;
	}

	@Override
	public ITypeID[] getLoopVariableTypes() {
		return TYPES;
	}

	@Override
	public <T> T acceptForIterator(ForeachIteratorVisitor<T> visitor) {
		return visitor.visitStringCharacterIterator();
	}

	@Override
	public CodePosition getPosition() {
		return CodePosition.BUILTIN;
	}

	@Override
	public String describe() {
		return "string character iterator";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		type.addIterator(new IteratorMemberRef(this, TYPES), priority);
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		throw new UnsupportedOperationException("Not a compilable member");
	}
}
