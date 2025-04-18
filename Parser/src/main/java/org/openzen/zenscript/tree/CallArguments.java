package org.openzen.zenscript.tree;

import org.openzen.zenscript.lexer.ZSTokenType;

class CallArguments {

	static void parse(TreeParser p) {
		if (p.at(ZSTokenType.T_BROPEN)) {
			TreeParser.MarkOpened open = p.open();
			p.expect(ZSTokenType.T_BROPEN);
			p.whitespace();
			if (!p.eat(ZSTokenType.T_BRCLOSE)) {
				do {
					// either eats nothing or eat for the comma
					p.whitespace();
					TreeParser.MarkOpened argumentOpen = p.open();
					Expression.parse(p);
					p.close(argumentOpen, TreeKind.CALL_ARGUMENT);
				} while (p.eat(ZSTokenType.T_COMMA) && !p.eof());
				p.expect(ZSTokenType.T_BRCLOSE);
				p.whitespace();
			}
			p.whitespace();
			p.close(open, TreeKind.CALL_ARGUMENTS);
		}
	}

}
