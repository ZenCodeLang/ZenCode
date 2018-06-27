/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedOperator extends ParsedFunctionalMember {
	private final OperatorType operator;
	private final ParsedFunctionHeader header;
	
	public ParsedOperator(CodePosition position, HighLevelDefinition definition, int modifiers, OperatorType operator, ParsedFunctionHeader header, ParsedFunctionBody body) {
		super(position, definition, modifiers, body);
		
		this.operator = operator;
		this.header = header;
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new OperatorMember(position, definition, modifiers, operator, header.compile(scope), null);
	}
}
