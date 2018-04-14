/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class DefinitionMember implements IDefinitionMember {
	public final CodePosition position;
	public final int modifiers;
	
	public DefinitionMember(CodePosition position, int modifiers) {
		this.position = position;
		this.modifiers = modifiers;
	}
	
	@Override
	public final CodePosition getPosition() {
		return position;
	}
	
	public boolean isStatic() {
		return Modifiers.isStatic(modifiers);
	}
}
