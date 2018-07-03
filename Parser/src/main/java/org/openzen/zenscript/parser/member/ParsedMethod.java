/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedMethod extends ParsedFunctionalMember {
	private final String name;
	private final ParsedFunctionHeader header;
	
	public ParsedMethod(CodePosition position, HighLevelDefinition definition, int modifiers, ParsedAnnotation[] annotations, String name, ParsedFunctionHeader header, ParsedFunctionBody body) {
		super(position, definition, modifiers, annotations, body);
		
		this.name = name;
		this.header = header;
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new MethodMember(position, definition, modifiers, name, header.compile(scope), null);
	}
}
