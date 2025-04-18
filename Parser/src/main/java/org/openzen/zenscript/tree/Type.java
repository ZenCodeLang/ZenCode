package org.openzen.zenscript.tree;

import org.openzen.zenscript.lexer.ZSTokenSet;
import org.openzen.zenscript.lexer.ZSTokenType;

import static org.openzen.zenscript.lexer.ZSTokenType.*;

class Type {

	static boolean parse(TreeParser p, ZSTokenSet recovery) {
		if (parse(p)) {
			return true;
		}
		p.recover("expected type", recovery);
		return false;
	}

	static boolean parse(TreeParser p) {

		ZSTokenType[] typeTypes = {K_VOID, K_BOOL, K_BYTE, K_SBYTE, K_SHORT, K_USHORT, K_INT, K_UINT, K_LONG, K_ULONG, K_USIZE, K_FLOAT, K_DOUBLE, K_CHAR, K_STRING};
		TreeParser.MarkClosed base;
		if (p.atAny(typeTypes)) {
			TreeParser.MarkOpened open = p.open();
			p.expect(typeTypes);
			base = p.close(open, TreeKind.TYPE);
			p.whitespace();
		} else if (p.at(T_BROPEN)) {
			TreeParser.MarkOpened open = p.open();
			p.expect(T_BROPEN);
			p.whitespace();
			Type.parse(p);
			p.expect(T_BRCLOSE);
			base = p.close(open, TreeKind.TYPE);
			p.whitespace();
		} else if (p.at(K_FUNCTION)) {
			TreeParser.MarkOpened open = p.open();
			p.expect(K_FUNCTION);
			p.whitespace();
			FunctionHeader.parse(p);
			base = p.close(open, TreeKind.TYPE);
		} else if (p.at(T_IDENTIFIER)) {
			TreeParser.MarkOpened open = p.open();
			p.name();
			Generics.parseTypeArguments(p);
			while (p.eat(T_DOT)) {
				p.whitespace();
				p.name();
				Generics.parseTypeArguments(p);
			}
			base = p.close(open, TreeKind.TYPE);
		} else {
			return false;
		}

		while (p.atAny(T_DOT2, T_SQOPEN, T_QUEST)) {
			TreeParser.MarkOpened open;
			switch (p.nth(0)) {
				case T_DOT2:
					open = p.openBefore(base);
					p.expect(T_DOT2);
					p.whitespace();
					Type.parse(p);
					base = p.close(open, TreeKind.RANGE);
					break;
				case T_SQOPEN:
					open = p.openBefore(base);
					p.expect(T_SQOPEN);
					p.whitespace();
					while (p.at(T_COMMA)) {
						p.expect(T_COMMA);
						p.whitespace();
					}
					if (p.at(T_SQCLOSE)) {
						p.expect(T_SQCLOSE);
						p.whitespace();
						base = p.close(open, TreeKind.ARRAY);
					} else if (p.at(T_LESS)) {
						p.expect(T_LESS);
						p.whitespace();
						Generics.parseTypeParameters(p);
						p.expect(T_GREATER);
						p.whitespace();
						p.expect(T_SQCLOSE);
						p.whitespace();
						base = p.close(open, TreeKind.GENERIC_MAP);
					} else {
						Type.parse(p);
						p.expect(T_SQCLOSE);
						p.whitespace();
						base = p.close(open, TreeKind.MAP);
					}
					break;
				case T_QUEST:
					open = p.openBefore(base);
					p.expect(T_QUEST);
					p.whitespace();
					base = p.close(open, TreeKind.OPTIONAL);
					break;
			}
		}
		p.whitespace();
		return base != null;
		// TODO error recovery if needed
//		advanceWithError("Expected type, found " + this.tokens.get(pos));
	}

	static boolean parseDeclaration(TreeParser p) {
		if (p.at(K_AS) || p.at(T_COLON)) {
			TreeParser.MarkOpened typeOpen = p.open();
			p.expect(K_AS, T_COLON);
			p.whitespace();
			Type.parse(p);
			p.close(typeOpen, TreeKind.TYPE_DECLARATION);
			return true;
		}
		return false;
	}

	static boolean parseDeclaration(TreeParser p, ZSTokenSet recovery) {
		if (p.at(K_AS) || p.at(T_COLON)) {
			TreeParser.MarkOpened typeOpen = p.open();
			p.expect(K_AS, T_COLON);
			p.whitespace();
			Type.parse(p, recovery);
			p.close(typeOpen, TreeKind.TYPE_DECLARATION);
			return true;
		}
		return false;
	}

}
