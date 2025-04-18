package org.openzen.zenscript.tree.lexer;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;

import java.util.HashMap;
import java.util.Map;

import static org.openzen.zenscript.lexer.ZSTokenType.T_IDENTIFIER;

public class ZSPosTokenFactory implements PositionalTokenFactory<ZSTokenType, ZSToken> {
	private static final Map<String, ZSToken> KEYWORDS = new HashMap<>();

	static {
		for (ZSTokenType type : ZSTokenType.values())
			if (type.isKeyword)
				KEYWORDS.put(type.flyweight.content, type.flyweight);
	}

	@Override
	public PositionedToken<ZSTokenType, ZSToken> create(CodePosition position, ZSTokenType type, String content) {
		if (type == T_IDENTIFIER && KEYWORDS.containsKey(content))
			return new PositionedToken<>(position, KEYWORDS.get(content));
		else if (type.flyweight != null)
			return new PositionedToken<>(position, type.flyweight);

		return new PositionedToken<>(position, new ZSToken(type, content));
	}
}
