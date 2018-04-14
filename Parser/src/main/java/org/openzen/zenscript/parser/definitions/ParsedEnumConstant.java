/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenStream;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedEnumConstant {
	public static ParsedEnumConstant parse(ZSTokenStream tokens) {
		ZSToken name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected");
		List<ParsedExpression> arguments = new ArrayList<>();
		if (tokens.optional(ZSTokenType.T_BROPEN) != null) {
			do {
				arguments.add(ParsedExpression.parse(tokens));
			} while (tokens.optional(ZSTokenType.T_COMMA) != null);
			tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
		}
		
		return new ParsedEnumConstant(name.position, name.content, arguments);
	}
	
	public final CodePosition position;
	public final String name;
	public final List<ParsedExpression> arguments;
	
	private final EnumConstantMember compiled;
	
	public ParsedEnumConstant(CodePosition position, String name, List<ParsedExpression> arguments) {
		this.position = position;
		this.name = name;
		this.arguments = arguments;
		
		compiled = new EnumConstantMember(position, name);
	}
}
