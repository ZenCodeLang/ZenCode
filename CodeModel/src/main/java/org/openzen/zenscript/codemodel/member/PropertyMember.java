/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class PropertyMember extends DefinitionMember {
	public ITypeID type;
	public final BuiltinID builtin;
	
	public PropertyMember(CodePosition position, HighLevelDefinition definition, int modifiers, ITypeID type, BuiltinID builtin) {
		super(position, definition, modifiers);
		
		this.type = type;
		this.builtin = builtin;
	}
	
	public ITypeID getType() {
		return type;
	}
	
	@Override
	public BuiltinID getBuiltin() {
		return builtin;
	}
}
