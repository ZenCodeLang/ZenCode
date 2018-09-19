/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.expression.ParsedCallArguments;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStorageTag {
	public static ParsedStorageTag parse(ZSTokenParser parser) {
		if (parser.optional(ZSTokenType.T_BACKTICK) == null)
			return null;
		
		String name = parser.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		ParsedCallArguments arguments = null;
		if (parser.peek().type == ZSTokenType.T_BROPEN)
			arguments = ParsedCallArguments.parse(parser);
		
		return new ParsedStorageTag(name, arguments);
	}
	
	public String name;
	public ParsedCallArguments arguments;
	
	public ParsedStorageTag(String name, ParsedCallArguments arguments) {
		this.name = name;
		this.arguments = arguments;
	}
}
