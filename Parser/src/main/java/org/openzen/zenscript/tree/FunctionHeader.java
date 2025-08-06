package org.openzen.zenscript.tree;

import org.openzen.zenscript.lexer.ZSTokenSet;
import org.openzen.zenscript.lexer.ZSTokenType;

class FunctionHeader {

	static final ZSTokenType[] FUNCTION_STARTS = {ZSTokenType.T_BROPEN, ZSTokenType.T_LESS};

	static void parse(TreeParser p) {
		parse(p, ZSTokenSet.empty());
	}

	static void parse(TreeParser p, ZSTokenSet recovery) {
		if (p.atAny(FUNCTION_STARTS)) {
			TreeParser.MarkOpened open = p.open();
			Generics.parseAllTypeParameters(p);
			TreeParser.MarkOpened paramListOpen = p.open();
			p.expect(ZSTokenType.T_BROPEN);
			p.whitespace();
			if (!p.eat(ZSTokenType.T_BRCLOSE)) {

				do {
					p.whitespace();
					TreeParser.MarkOpened paramOpen = p.open();
					Annotation.parse(p);

					p.name(recovery);
					p.eat(ZSTokenType.T_DOT3);
					p.whitespace();
					Type.parseDeclaration(p, recovery);
					if (p.eat(ZSTokenType.T_ASSIGN)) {
						p.whitespace();
						if (Expression.parse(p) == null) {
							p.recover("expected expression", recovery);
						}
					}
					//TODO this will insert an empty param in an errored tree
					p.close(paramOpen, TreeKind.PARAM);
				} while (p.eat(ZSTokenType.T_COMMA));
				p.expect(ZSTokenType.T_BRCLOSE);
			}
			p.close(paramListOpen, TreeKind.PARAMS);
			p.whitespace();

			//TODO this error recovery is pretty rough
			Type.parseDeclaration(p, recovery);
			if (p.at(ZSTokenType.K_THROWS)) {
				TreeParser.MarkOpened throwsOpen = p.open();
				p.expect(ZSTokenType.K_THROWS);
				p.whitespace();
				Type.parse(p, recovery);
				p.close(throwsOpen, TreeKind.THROWS);
			}
			p.close(open, TreeKind.FUNCTION_HEADER);
		} else {
			p.recover("expected a function header", recovery);
		}
	}

}
