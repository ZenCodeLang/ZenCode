/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import java.util.HashMap;
import java.util.Map;
import static org.openzen.zenscript.lexer.ZSTokenType.*;

/**
 *
 * @author Hoofdgebruiker
 */
public class ZSTokenFactory implements TokenFactory<ZSToken, ZSTokenType> {
	public static final ZSTokenFactory INSTANCE = new ZSTokenFactory();
	
	private static final Map<String, ZSToken> KEYWORDS = new HashMap<>();
	
	static {
		for (ZSTokenType type : ZSTokenType.values())
			if (type.isKeyword)
				KEYWORDS.put(type.flyweight.content, type.flyweight);
	}

	@Override
	public ZSToken create(ZSTokenType type, String content) {
		if (type == T_IDENTIFIER && KEYWORDS.containsKey(content))
			return KEYWORDS.get(content);
		else if (type.flyweight != null)
			return type.flyweight;
		
		return new ZSToken(type, content);
	}
}
