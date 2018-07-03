/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedGetter extends ParsedFunctionalMember {
	private final String name;
	private final IParsedType type;
	
	public ParsedGetter(CodePosition position, HighLevelDefinition definition, int modifiers, ParsedAnnotation[] annotations, String name, IParsedType type, ParsedFunctionBody body) {
		super(position, definition, modifiers, annotations, body);
		
		this.name = name;
		this.type = type;
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new GetterMember(position, definition, modifiers, name, type.compile(scope), null);
	}
}
