/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedCaster extends ParsedFunctionalMember {
	private final IParsedType type;
	
	public ParsedCaster(CodePosition position, HighLevelDefinition definition, int modifiers, ParsedAnnotation[] annotations, IParsedType type, ParsedFunctionBody body) {
		super(position, definition, modifiers, annotations, body);
		
		this.type = type;
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new CasterMember(position, definition, modifiers, type.compile(scope), null);
	}
}
