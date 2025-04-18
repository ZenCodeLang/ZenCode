package org.openzen.zenscript.tree;

import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenSet;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.lexer.PositionedToken;

import java.util.ArrayList;
import java.util.List;

import static org.openzen.zenscript.lexer.ZSTokenType.*;

class Generics {

	static final ZSTokenSet GENERIC_RECOVERY_SET = Definition.DEFINITION_RECOVERY_SET.and(T_GREATER, T_AOPEN, T_COMMA);
//TODO error recovery
	static void parseTypeArguments(TreeParser p) {
		if (p.at(T_LESS)) {
			TreeParser subParser = p.subTree();

			TreeParser.MarkOpened open = subParser.open();

			subParser.expect(T_LESS);

			do {
				// Either eats the whitespace after the less, or after the comma
				subParser.whitespace();
				if (!Type.parse(subParser)) {
					return;
				}
				subParser.whitespace();
			} while (subParser.eat(T_COMMA) && !subParser.eof());

			subParser.replace(token -> {

				List<PositionedToken<ZSTokenType, ZSToken>> newTokens = new ArrayList<>();
				ZSTokenType type = token.getType();
				if (type == T_SHR) {
					newTokens.add(new PositionedToken<>(token.position().withLength(1), T_GREATER.flyweight));
					newTokens.add(new PositionedToken<>(token.position().subPosition(1), T_GREATER.flyweight));
				} else if (type == T_USHR) {
					newTokens.add(new PositionedToken<>(token.position().withLength(1), T_GREATER.flyweight));
					newTokens.add(new PositionedToken<>(token.position().subPosition(1), T_SHR.flyweight));
				} else if (type == T_SHRASSIGN) {
					newTokens.add(new PositionedToken<>(token.position().withLength(1), T_GREATER.flyweight));
					newTokens.add(new PositionedToken<>(token.position().subPosition(1), T_GREATEREQ.flyweight));
				} else if (type == T_USHRASSIGN) {
					newTokens.add(new PositionedToken<>(token.position().withLength(1), T_GREATER.flyweight));
					newTokens.add(new PositionedToken<>(token.position().subPosition(1), T_SHRASSIGN.flyweight));
				}
				return newTokens;
			});
			if (!subParser.at(T_GREATER)) {
				return;
			}
			subParser.expect(T_GREATER);
			subParser.whitespace();
			subParser.close(open, TreeKind.TYPE_PARAMS);
			p.mergeIn(subParser);

		}
	}

	static void parseAllTypeParameters(TreeParser p) {

		if (p.at(T_LESS)) {
			TreeParser.MarkOpened open = p.open();
			p.expect(T_LESS);
			p.whitespace();

			while (!p.at(T_GREATER) && !p.eof()) {
				if (!parseTypeParameters(p)) {
					break;
				}
			}
			p.expect(T_GREATER);
			p.whitespace();

			p.close(open, TreeKind.TYPE_PARAMS);
		}
	}

	static boolean parseTypeParameters(TreeParser p) {

		if (p.at(T_IDENTIFIER)) {
			TreeParser.MarkOpened open = p.open();
			p.name(GENERIC_RECOVERY_SET);
			p.whitespace();

			p.whileAt(T_COLON, () -> {
				TreeParser.MarkOpened boundOpen = p.open();
				p.expect(T_COLON);
				p.whitespace();
				if (p.at(K_SUPER)) {
					p.expect(K_SUPER);
					p.whitespace();
					Type.parse(p, GENERIC_RECOVERY_SET);
					p.whitespace();
					p.close(boundOpen, TreeKind.SUPER_BOUND);
				} else {
					Type.parse(p, GENERIC_RECOVERY_SET);
					p.whitespace();
					// TODO parse type, if not found, break out of the top loop and recover
					p.close(boundOpen, TreeKind.TYPE_BOUND);
				}
			});
			p.close(open, TreeKind.TYPE_PARAM);
			if (!p.at(T_GREATER)) {
				if(p.eat(T_COMMA)) {
					p.whitespace();
				} else {
					//TODO is this error message right?
					p.recover("expected ',' or '>'", GENERIC_RECOVERY_SET);
					return false;
				}
			}
			return true;
		}

		p.recover("expected type parameter name", GENERIC_RECOVERY_SET);
		return false;
	}
}
