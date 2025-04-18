package org.openzen.zenscript.tree;

import org.openzen.zenscript.lexer.ZSTokenType;

class Annotation {

	//TODO TEST
	static boolean parse(TreeParser p) {
		boolean parsed = false;
		while (p.at(ZSTokenType.T_SQOPEN)) {
			if (p.nth(1) == ZSTokenType.T_IDENTIFIER) {
				TreeParser.MarkOpened open = p.open();
				p.expect(ZSTokenType.T_SQOPEN);
				p.whitespace();
				Type.parse(p);
				p.whitespace();
				CallArguments.parse(p);
				p.whitespace();
				p.expect(ZSTokenType.T_SQCLOSE);
				p.close(open, TreeKind.ANNOTATION);
				p.whitespace();
				parsed = true;
			} else {
				break;
			}
		}
		return parsed;
	}
}
