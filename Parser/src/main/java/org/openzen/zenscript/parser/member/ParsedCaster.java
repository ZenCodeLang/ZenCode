/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedCaster extends ParsedFunctionalMember {
	private final IParsedType type;
	
	public ParsedCaster(CodePosition position, int modifiers, IParsedType type, ParsedFunctionBody body) {
		super(position, modifiers, body);
		
		this.type = type;
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new CasterMember(position, modifiers, type.compile(scope));
	}
}
