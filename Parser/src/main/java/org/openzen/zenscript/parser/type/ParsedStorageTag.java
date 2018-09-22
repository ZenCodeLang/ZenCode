/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.storage.SharedStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStorageTag {
	public static final ParsedStorageTag NULL = new ParsedStorageTag("", null);
	
	public static ParsedStorageTag parse(ZSTokenParser parser) {
		if (parser.optional(ZSTokenType.T_BACKTICK) == null)
			return NULL;
		
		String name = parser.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<String> arguments = null;
		if (parser.optional(ZSTokenType.T_BROPEN) != null) {
			arguments = new ArrayList<>();
			if (parser.optional(ZSTokenType.T_BRCLOSE) == null) {
				do {
					arguments.add(parser.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content);
				} while (parser.optional(ZSTokenType.T_COMMA) == null);
				parser.required(ZSTokenType.T_BRCLOSE, ") expected");
			}
		}
		
		return new ParsedStorageTag(name, arguments.toArray(new String[arguments.size()]));
	}
	
	public String name;
	public String[] arguments;
	
	public ParsedStorageTag(String name, String[] arguments) {
		this.name = name;
		this.arguments = arguments;
	}
	
	public StorageTag resolve(CodePosition position, TypeResolutionContext context) {
		if (this == NULL)
			return SharedStorageTag.INSTANCE;
		
		return context.getStorageTag(position, name, arguments);
	}
}
