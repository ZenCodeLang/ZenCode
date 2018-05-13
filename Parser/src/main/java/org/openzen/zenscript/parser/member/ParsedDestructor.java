/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedDestructor extends ParsedFunctionalMember {
	public ParsedDestructor(CodePosition position, HighLevelDefinition definition, int modifiers, ParsedFunctionBody body) {
		super(position, definition, modifiers, body);
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new DestructorMember(position, definition, modifiers);
	}
}
