package org.openzen.zenscript.tree;

import org.openzen.zenscript.lexer.ZSTokenSet;
import org.openzen.zenscript.lexer.ZSTokenType;

class Import {

	static final ZSTokenSet IMPORT_RECOVERY = ZSTokenSet.of(ZSTokenType.K_IMPORT, ZSTokenType.T_SEMICOLON);

	static boolean parse(TreeParser p) {
		if (p.at(ZSTokenType.K_IMPORT)) {
			TreeParser.MarkOpened open = p.open();
			p.eat(ZSTokenType.K_IMPORT);
			p.whitespace();
			p.eat(ZSTokenType.T_DOT);
			p.whitespace();

			do {
				p.name(IMPORT_RECOVERY);
			} while (p.eat(ZSTokenType.T_DOT));
			p.whitespace();

			if (p.eat(ZSTokenType.K_AS)) {
				p.whitespace();
				p.name(IMPORT_RECOVERY);
				p.whitespace();
			}
			p.expect(ZSTokenType.T_SEMICOLON);
			p.close(open, TreeKind.IMPORT);
			p.whitespace();
			return true;
		}
		return false;
	}
}
