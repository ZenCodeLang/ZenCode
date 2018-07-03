/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedConstructor extends ParsedFunctionalMember {
	private final ParsedFunctionHeader header;
	
	public ParsedConstructor(CodePosition position, HighLevelDefinition definition, int modifiers, ParsedAnnotation[] annotations, ParsedFunctionHeader header, ParsedFunctionBody body) {
		super(position, definition, modifiers, annotations, body);
		
		this.header = header;
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new ConstructorMember(position, definition, modifiers, header.compile(scope), null);
	}
}
