/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class PropertyMember extends DefinitionMember {
	public StoredType type;
	public final BuiltinID builtin;
	
	public PropertyMember(CodePosition position, HighLevelDefinition definition, int modifiers, StoredType type, BuiltinID builtin) {
		super(position, definition, modifiers);
		
		this.type = type;
		this.builtin = builtin;
	}
	
	public StoredType getType() {
		return type;
	}
	
	@Override
	public BuiltinID getBuiltin() {
		return builtin;
	}
}
