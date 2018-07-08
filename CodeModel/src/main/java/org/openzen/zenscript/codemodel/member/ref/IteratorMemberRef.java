/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.IIteratorMember;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class IteratorMemberRef implements DefinitionMemberRef {
	public final IIteratorMember target;
	public final ITypeID[] types;
	
	public IteratorMemberRef(IIteratorMember target, ITypeID... types) {
		this.target = target;
		this.types = types;
	}

	@Override
	public CodePosition getPosition() {
		return target.getPosition();
	}

	@Override
	public String describe() {
		return target.describe();
	}

	@Override
	public <T> T getTag(Class<T> type) {
		return target.getTag(type);
	}
	
	public int getLoopVariableCount() {
		return types.length;
	}
}
