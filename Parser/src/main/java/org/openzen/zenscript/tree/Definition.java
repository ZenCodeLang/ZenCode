package org.openzen.zenscript.tree;

import org.openzen.zenscript.lexer.ZSTokenSet;

import static org.openzen.zenscript.lexer.ZSTokenType.*;

class Definition {

	static final ZSTokenSet DEFINITION_STARTS = ZSTokenSet.of(K_CLASS, K_INTERFACE, K_ENUM, K_STRUCT, K_ALIAS, K_FUNCTION, K_EXPAND, K_VARIANT);

	static final ZSTokenSet DEFINITION_RECOVERY_SET = ZSTokenSet.of(K_CLASS, K_INTERFACE, K_ENUM, K_STRUCT, K_ALIAS, K_FUNCTION, K_EXPAND, K_VARIANT, T_SEMICOLON);

	static boolean parse(TreeParser p) {
		return parse(p, Modifiers.parseDefinitionModifiers(p));
	}

	static boolean parse(TreeParser p, TreeParser.MarkClosed modifiers) {

		if (p.atAny(DEFINITION_STARTS)) {
			if (p.at(K_CLASS)) {
				parseClass(p, modifiers);
				return true;
			} else if (p.at(K_INTERFACE)) {
				parseInterface(p, modifiers);
				return true;
			} else if (p.at(K_ENUM)) {
				parseEnum(p, modifiers);
				return true;
			} else if (p.at(K_STRUCT)) {
				parseStruct(p, modifiers);
				return true;
			} else if (p.at(K_ALIAS)) {
				parseAlias(p, modifiers);
				return true;
			} else if (p.at(K_FUNCTION)) {
				parseFunction(p, modifiers);
				return true;
			} else if (p.at(K_EXPAND)) {
				parseExpansion(p, modifiers);
				return true;
			} else if (p.at(K_VARIANT)) {
				parseVariant(p, modifiers);
				return true;
			}
		}
		return false;
	}

	static void parseClass(TreeParser p, TreeParser.MarkClosed modifiers) {
		TreeParser.MarkOpened open = p.openBefore(modifiers);
		p.expect(K_CLASS);
		p.whitespace();
		p.name(DEFINITION_RECOVERY_SET);
		Generics.parseAllTypeParameters(p);

		if (p.at(T_COLON)) {
			TreeParser.MarkOpened superType = p.open();
			p.expect(T_COLON);
			p.whitespace();
			Type.parse(p, DEFINITION_RECOVERY_SET);
			p.close(superType, TreeKind.SUPER_TYPE);
		}

		if (p.at(T_AOPEN)) {
			TreeParser.MarkOpened defMembersOpen = p.open();
			p.expect(T_AOPEN);
			p.whitespace();
			while (!p.at(T_ACLOSE) && !p.eof()) {
				// This may need a way to fail the close tree?
				DefinitionMembers.parse(p);
			}
			p.expect(T_ACLOSE);
			p.whitespace();
			p.close(defMembersOpen, TreeKind.MEMBERS);
		} else {
			p.recover("expected '{'", DEFINITION_RECOVERY_SET);
		}

		p.close(open, TreeKind.DEF_CLASS);
	}

	static void parseInterface(TreeParser p, TreeParser.MarkClosed modifiers) {
		TreeParser.MarkOpened open = p.openBefore(modifiers);
		p.expect(K_INTERFACE);
		p.whitespace();
		p.name(DEFINITION_RECOVERY_SET);
		Generics.parseAllTypeParameters(p);

		if (p.at(T_COLON)) {
			TreeParser.MarkOpened superTypes = p.open();
			p.expect(T_COLON);

			do {
				// eat from after the colon or the comma
				p.whitespace();
				TreeParser.MarkOpened superType = p.open();
				Type.parse(p, DEFINITION_RECOVERY_SET);
				p.close(superType, TreeKind.SUPER_TYPE);
			} while (p.eat(T_COMMA));

			p.close(superTypes, TreeKind.SUPER_TYPES);
		}

		if (p.at(T_AOPEN)) {
			TreeParser.MarkOpened defMembersOpen = p.open();
			p.expect(T_AOPEN);
			p.whitespace();
			while (!p.at(T_ACLOSE) && !p.eof()) {
				// This may need a way to fail the close tree?
				DefinitionMembers.parse(p);
			}
			p.expect(T_ACLOSE);
			p.close(defMembersOpen, TreeKind.MEMBERS);
			p.whitespace();
		} else {
			p.recover("expected '{'", DEFINITION_RECOVERY_SET);
		}

		p.close(open, TreeKind.DEF_INTERFACE);
	}

	static void parseEnum(TreeParser p, TreeParser.MarkClosed modifiers) {
		TreeParser.MarkOpened open = p.openBefore(modifiers);
		p.expect(K_ENUM);
		p.whitespace();
		p.name(DEFINITION_RECOVERY_SET);

		if (p.atAny(K_AS, T_COLON)) {
			TreeParser.MarkOpened valueTypeOpen = p.open();
			p.expect(K_AS, T_COLON);
			p.whitespace();
			Type.parse(p, DEFINITION_RECOVERY_SET);
			p.close(valueTypeOpen, TreeKind.TYPE_DECLARATION);
		}

		if (p.at(T_AOPEN)) {
			p.eat(T_AOPEN);
			p.whitespace();
			while (!p.at(T_ACLOSE) && !p.at(T_SEMICOLON) && !p.eof()) {

				DefinitionMembers.parseEnumConstant(p);
				if (!p.eat(T_COMMA)) {
					break;
				}
				p.whitespace();
			}


			if (p.at(T_SEMICOLON)) {
				p.eat(T_SEMICOLON);
				p.whitespace();
				while (!p.eat(T_ACLOSE)) {
					DefinitionMembers.parse(p);
				}
				p.whitespace();
			} else {
				p.expect(T_ACLOSE);
				p.whitespace();
			}

		} else {
			p.recover("expected '{'", DEFINITION_RECOVERY_SET);
		}
		p.close(open, TreeKind.DEF_ENUM);
	}


	static void parseStruct(TreeParser p, TreeParser.MarkClosed modifiers) {
		TreeParser.MarkOpened open = p.openBefore(modifiers);
		p.expect(K_STRUCT);
		p.whitespace();
		p.name(DEFINITION_RECOVERY_SET);
		Generics.parseAllTypeParameters(p);

		if (p.at(T_AOPEN)) {
			TreeParser.MarkOpened defMembersOpen = p.open();
			p.expect(T_AOPEN);
			p.whitespace();
			while (!p.at(T_ACLOSE) && !p.eof()) {
				// This may need a way to fail the close tree?
				DefinitionMembers.parse(p);
			}
			p.expect(T_ACLOSE);
			p.whitespace();
			p.close(defMembersOpen, TreeKind.MEMBERS);
		} else {
			p.recover("expected '{'", DEFINITION_RECOVERY_SET);
		}

		p.close(open, TreeKind.DEF_STRUCT);
	}


	static void parseAlias(TreeParser p, TreeParser.MarkClosed modifiers) {
		TreeParser.MarkOpened open = p.openBefore(modifiers);
		p.expect(K_ALIAS);
		p.whitespace();
		p.name(DEFINITION_RECOVERY_SET);
		Generics.parseAllTypeParameters(p);
		//TODO in the old parser, this wouldn't accept T_COLON, now it would without this
		if (p.at(K_AS)) {
			Type.parseDeclaration(p, DEFINITION_RECOVERY_SET);
			p.expect(T_SEMICOLON);
			p.whitespace();
		} else {
			p.recover("expected as", DEFINITION_RECOVERY_SET);
		}

		p.close(open, TreeKind.DEF_ALIAS);
	}

	static void parseFunction(TreeParser p, TreeParser.MarkClosed modifiers) {
		TreeParser.MarkOpened open = p.openBefore(modifiers);
		p.expect(K_FUNCTION);
		p.whitespace();
		p.name(DEFINITION_RECOVERY_SET);

		FunctionHeader.parse(p, DEFINITION_RECOVERY_SET);
		Statement.functionBody(p, DEFINITION_RECOVERY_SET);
		p.close(open, TreeKind.DEF_FUNCTION);
	}

	static void parseExpansion(TreeParser p, TreeParser.MarkClosed modifiers) {
		TreeParser.MarkOpened open = p.openBefore(modifiers);
		p.expect(K_EXPAND);
		p.whitespace();
		Generics.parseAllTypeParameters(p);

		if (Type.parse(p, DEFINITION_RECOVERY_SET)) {
			TreeParser.MarkOpened defMembersOpen = p.open();
			p.expect(T_AOPEN);
			p.whitespace();
			while (!p.at(T_ACLOSE) && !p.eof()) {
				// This may need a way to fail the close tree?
				DefinitionMembers.parse(p);
			}
			p.expect(T_ACLOSE);
			p.whitespace();
			p.close(defMembersOpen, TreeKind.MEMBERS);
		}

		p.close(open, TreeKind.DEF_EXPANSION);
	}

	static void parseVariant(TreeParser p, TreeParser.MarkClosed modifiers) {
		TreeParser.MarkOpened open = p.openBefore(modifiers);
		p.expect(K_VARIANT);
		p.whitespace();
		p.name(DEFINITION_RECOVERY_SET);
		Generics.parseAllTypeParameters(p);

		if (p.at(T_AOPEN)) {
			TreeParser.MarkOpened defMembersOpen = p.open();
			p.expect(T_AOPEN);
			p.whitespace();
			//TODO need a test for variant Foo {;}
			while (!p.at(T_ACLOSE) && !p.at(T_SEMICOLON) && !p.eof()) {
				TreeParser.MarkOpened optionOpen = p.open();
				//TODO test this error recovery
				p.name(DEFINITION_RECOVERY_SET);

				if (p.at(T_BROPEN)) {
					TreeParser.MarkOpened optionTypes = p.open();
					p.expect(T_BROPEN);
					do {
						p.whitespace();
						Type.parse(p, DEFINITION_RECOVERY_SET);
					} while (p.eat(T_COMMA));

					p.expect(T_BRCLOSE);
					p.close(optionTypes, TreeKind.VARIANT_OPTION_TYPES);
				}
				p.close(optionOpen, TreeKind.VARIANT_OPTION);
				p.whitespace();
				if (!p.eat(T_COMMA)) {
					break;
				}
				p.whitespace();
			}

			if (p.eat(T_SEMICOLON)) {
				p.whitespace();
				while (!p.at(T_ACLOSE) && !p.eof()) {
					// This may need a way to fail the close tree?
					DefinitionMembers.parse(p);
				}
				p.eat(T_ACLOSE);
				p.whitespace();
			} else {
				p.expect(T_ACLOSE);
				p.whitespace();
			}
			p.close(defMembersOpen, TreeKind.MEMBERS);
		} else {
			p.recover("expected '{'", DEFINITION_RECOVERY_SET);
		}
		p.close(open, TreeKind.DEF_VARIANT);
	}
}
