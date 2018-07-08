/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedCaller extends ParsedFunctionalMember {
	private final ParsedFunctionHeader header;
	
	public ParsedCaller(CodePosition position, HighLevelDefinition definition, int modifiers, ParsedAnnotation[] annotations, ParsedFunctionHeader header, ParsedFunctionBody body) {
		super(position, definition, modifiers, annotations, body);
		
		this.header = header;
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new CallerMember(position, definition, modifiers, header.compile(scope), null);
	}
}
