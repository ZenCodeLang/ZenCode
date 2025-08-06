package org.openzen.zenscript.tree;

import org.openzen.zenscript.lexer.ZSTokenSet;
import org.openzen.zenscript.lexer.ZSTokenType;

import static org.openzen.zenscript.lexer.ZSTokenType.*;

class DefinitionMembers {

	static final ZSTokenSet MEMBER_STARTS = ZSTokenSet.of(
			K_VAL,
			K_VAR,
			K_THIS,
			T_IDENTIFIER,
			K_SET,
			K_GET,
			K_IMPLEMENTS,
			T_BROPEN,
			T_SQOPEN,
			T_CAT,
			T_ADD,
			T_SUB,
			T_MUL,
			T_DIV,
			T_MOD,
			T_AND,
			T_OR,
			T_XOR,
			T_NOT,
			T_ADDASSIGN,
			T_SUBASSIGN,
			T_CATASSIGN,
			T_MULASSIGN,
			T_DIVASSIGN,
			T_MODASSIGN,
			T_ANDASSIGN,
			T_ORASSIGN,
			T_XORASSIGN,
			T_INCREMENT,
			T_DECREMENT,
			T_DOT2,
			T_SHL,
			T_SHR,
			T_USHR,
			T_SHLASSIGN,
			T_SHRASSIGN,
			T_USHRASSIGN,
			T_EQUAL2,
			K_AS,
			K_IN,
			K_CLASS,
			K_INTERFACE,
			K_ALIAS,
			K_STRUCT,
			K_ENUM,
			K_FOR
	);
	static final ZSTokenSet ENUM_CONSTANT_RECOVERY = ZSTokenSet.of(T_COMMA, T_SEMICOLON, T_ACLOSE);
	static final ZSTokenSet FIELD_RECOVERY = MEMBER_STARTS.and(K_AS, T_COLON, T_ASSIGN);

	// contains function body starts
	static final ZSTokenSet FUNCTION_MEMBER_RECOVERY = MEMBER_STARTS.and(T_LAMBDA, T_SEMICOLON, T_AOPEN);

	static void parse(TreeParser p) {
		Modifiers.ModifierClose memberModifiers = Modifiers.parseDefinitionMemberModifiers(p);

		if (p.atAny(MEMBER_STARTS)) {
			// We don't know what it is if we eat it first
			ZSTokenType statementStart = p.nth(0);
			switch (statementStart) {
				case K_VAL:
				case K_VAR:
					parseField(p, memberModifiers);
					break;
				case K_THIS:
					parseThis(p, memberModifiers);
					break;
				case T_IDENTIFIER:
					parseMethod(p, memberModifiers);
					break;
				case K_SET:
					parseSetter(p, memberModifiers);
					break;
				case K_GET:
					parseGetter(p, memberModifiers);
					break;
				case K_IMPLEMENTS:
					parseImplementation(p, memberModifiers);
					break;
				case T_BROPEN:
					parseCallOperator(p, memberModifiers);
					break;
				case T_SQOPEN:
					parseIndexOperator(p, memberModifiers);
					break;
				case T_CAT:
					if (p.nth(1) == K_THIS) {
						parseDestructor(p, memberModifiers);
						break;
					}
				case T_ADD:
				case T_SUB:
				case T_MUL:
				case T_DIV:
				case T_MOD:
				case T_AND:
				case T_OR:
				case T_XOR:
				case T_NOT:
				case T_ADDASSIGN:
				case T_SUBASSIGN:
				case T_CATASSIGN:
				case T_MULASSIGN:
				case T_DIVASSIGN:
				case T_MODASSIGN:
				case T_ANDASSIGN:
				case T_ORASSIGN:
				case T_XORASSIGN:
				case T_INCREMENT:
				case T_DECREMENT:
				case T_DOT2:
				case T_SHL:
				case T_SHR:
				case T_USHR:
				case T_SHLASSIGN:
				case T_SHRASSIGN:
				case T_USHRASSIGN:
				case T_EQUAL2:
				case K_IN:
					parseOperator(p, memberModifiers);
					break;
				case K_AS:
					parseCaster(p, memberModifiers);
					break;
				case K_CLASS:
				case K_INTERFACE:
				case K_ALIAS:
				case K_STRUCT:
				case K_ENUM:
					Definition.parse(p, memberModifiers.close());
					break;
				default:
					if (memberModifiers.modifiers().isStatic() && p.at(T_AOPEN)) {
						TreeParser.MarkOpened open = p.open();
						Statement.block(p, memberModifiers.close());
						p.close(open, TreeKind.MEMB_STATIC_INITIALIZER);
					}
					break;
			}
			return;
		}
	}

	static void parseMethod(TreeParser p, Modifiers.ModifierClose modifiers) {
		if (p.at(T_IDENTIFIER)) {
			TreeParser.MarkOpened open = p.openBefore(modifiers.close());
			p.name(FUNCTION_MEMBER_RECOVERY);

			if (modifiers.modifiers().isConst() && (p.atAny(K_AS, T_ASSIGN))) {
				//TODO in the old parser, this wouldn't accept T_COLON, now it would
				Type.parseDeclaration(p, FUNCTION_MEMBER_RECOVERY);
				p.expect(T_ASSIGN);
				p.whitespace();
				if (Expression.parse(p) == null) {
					p.recover("expected expression", FUNCTION_MEMBER_RECOVERY);
				}
				p.expect(T_SEMICOLON);
				p.whitespace();

				p.close(open, TreeKind.MEMB_FIELD);
			} else {
				FunctionHeader.parse(p, FUNCTION_MEMBER_RECOVERY);
				Statement.functionBody(p, FUNCTION_MEMBER_RECOVERY);
				p.close(open, TreeKind.MEMB_METHOD);
			}
		}
	}

	static void parseThis(TreeParser p, Modifiers.ModifierClose close) {
		if (p.at(K_THIS)) {
			TreeParser.MarkOpened open = p.openBefore(close.close());
			p.expect(K_THIS);
			p.whitespace();
			FunctionHeader.parse(p, FUNCTION_MEMBER_RECOVERY);
			Statement.functionBody(p, FUNCTION_MEMBER_RECOVERY);
			p.close(open, TreeKind.MEMB_CONSTRUCTOR);
		}
	}

	static void parseField(TreeParser p, Modifiers.ModifierClose close) {
		if (p.atAny(K_VAL, K_VAR)) {
			TreeParser.MarkOpened open = p.openBefore(close.close());
			p.eat(K_VAL, K_VAR);
			p.whitespace();
			p.name(FIELD_RECOVERY);
			if (p.at(K_AS)) {
				Type.parseDeclaration(p, FIELD_RECOVERY);
			}
			if (p.at(T_COLON)) {
				TreeParser.MarkOpened autosOpen = p.open();
				p.expect(T_COLON);
				do {
					p.whitespace();
					TreeParser.MarkClosed modifierClose = null;
					if (p.at(K_PUBLIC) || p.at(K_PROTECTED)) {
						TreeParser.MarkOpened modifierOpen = p.open();
						p.eat(K_PUBLIC, K_PROTECTED);
						p.whitespace();
						modifierClose = p.close(modifierOpen, TreeKind.MODIFIERS);
					}

					if (p.at(K_GET)) {
						TreeParser.MarkOpened getOpen = p.openBefore(modifierClose);
						p.eat(K_GET);
						p.whitespace();
						p.close(getOpen, TreeKind.AUTO_GET);
					} else if (p.at(K_SET)) {
						TreeParser.MarkOpened setOpen = p.openBefore(modifierClose);
						p.eat(K_SET);
						p.whitespace();
						p.close(setOpen, TreeKind.AUTO_SET);
					} else {
						//TODO should this add an error tree?
						p.recover("expected auto", FIELD_RECOVERY);
					}
				} while (p.eat(T_COMMA));
				p.close(autosOpen, TreeKind.FIELD_AUTO);
			}

			if (p.at(T_ASSIGN)) {
				p.expect(T_ASSIGN);
				p.whitespace();
				if (Expression.parse(p) == null) {
					p.recover("expected expression", FIELD_RECOVERY);
				}
			}


			p.expect(T_SEMICOLON);
			p.close(open, TreeKind.MEMB_FIELD);
			p.whitespace();
		}
	}

	static void parseEnumConstant(TreeParser p) {
		TreeParser.MarkOpened open = p.open();

		//TODO Confirm
		p.name(ENUM_CONSTANT_RECOVERY);

		if (p.at(T_BROPEN)) {
			TreeParser.MarkOpened argumentsOpen = p.open();
			p.expect(T_BROPEN);
			p.whitespace();
			do {
				p.whitespace();
				TreeParser.MarkOpened argumentOpen = p.open();
				Expression.parse(p);
				p.close(argumentOpen, TreeKind.CALL_ARGUMENT);
				p.whitespace();
			} while (p.eat(T_COMMA));

			p.expect(T_BRCLOSE);
			p.close(argumentsOpen, TreeKind.CALL_ARGUMENTS);
			p.whitespace();
		}

		if (p.at(T_ASSIGN)) {
			TreeParser.MarkOpened constantValueOpen = p.open();
			p.expect(T_ASSIGN);
			p.whitespace();
			Expression.parse(p);
			p.close(constantValueOpen, TreeKind.ENUM_CONSTANT_VALUE);
		}

		p.close(open, TreeKind.ENUM_CONSTANT);
	}

	static void parseSetter(TreeParser p, Modifiers.ModifierClose close) {
		if (p.at(K_SET)) {
			TreeParser.MarkOpened open = p.openBefore(close.close());
			p.expect(K_SET);
			p.whitespace();
			// We would recover at K_AS, but this contains that already
			p.name(FUNCTION_MEMBER_RECOVERY);
			if (p.at(K_AS)) {
				Type.parseDeclaration(p, FUNCTION_MEMBER_RECOVERY);
			}
			Statement.functionBody(p, FUNCTION_MEMBER_RECOVERY);
			p.close(open, TreeKind.MEMB_SETTER);
		}
	}

	static void parseGetter(TreeParser p, Modifiers.ModifierClose close) {
		if (p.at(K_GET)) {
			TreeParser.MarkOpened open = p.openBefore(close.close());
			p.expect(K_GET);
			p.whitespace();
			p.name(FUNCTION_MEMBER_RECOVERY);
			if (p.at(K_AS)) {
				Type.parseDeclaration(p, FUNCTION_MEMBER_RECOVERY);
			}
			Statement.functionBody(p, FUNCTION_MEMBER_RECOVERY);
			p.close(open, TreeKind.MEMB_GETTER);
		}
	}

	static void parseImplementation(TreeParser p, Modifiers.ModifierClose close) {
		if (p.at(K_IMPLEMENTS)) {
			//TODO error recovery
			TreeParser.MarkOpened open = p.openBefore(close.close());
			p.expect(K_IMPLEMENTS);
			p.whitespace();
			Type.parse(p);

			if (!p.eat(T_SEMICOLON)) {
				p.expect(T_AOPEN);
				p.whitespace();
				while (!p.at(T_ACLOSE)) {
					parse(p);
				}
				p.expect(T_ACLOSE);
			}
			p.close(open, TreeKind.MEMB_IMPLEMENTS);
			p.whitespace();
		}
	}

	static void parseCallOperator(TreeParser p, Modifiers.ModifierClose close) {
		TreeParser.MarkOpened open = p.openBefore(close.close());
		FunctionHeader.parse(p, FUNCTION_MEMBER_RECOVERY);
		Statement.functionBody(p, FUNCTION_MEMBER_RECOVERY);
		p.close(open, TreeKind.MEMB_OPERATOR);
		p.whitespace();
	}

	static void parseIndexOperator(TreeParser p, Modifiers.ModifierClose close) {
		if (p.at(T_SQOPEN)) {
			TreeParser.MarkOpened open = p.openBefore(close.close());
			p.expect(T_SQOPEN);
			p.whitespace();
			p.expect(T_SQCLOSE);
			p.whitespace();
			// TODO does this need error recovery testing?
			p.eat(T_ASSIGN);
			p.whitespace();
			FunctionHeader.parse(p, FUNCTION_MEMBER_RECOVERY);
			Statement.functionBody(p, FUNCTION_MEMBER_RECOVERY);
			p.close(open, TreeKind.MEMB_OPERATOR);
		}
	}

	static void parseDestructor(TreeParser p, Modifiers.ModifierClose close) {
		if (p.at(T_CAT) && p.nth(1) == K_THIS) {
			TreeParser.MarkOpened open = p.openBefore(close.close());
			p.expect(T_CAT);
			p.whitespace();
			p.expect(K_THIS);
			p.whitespace();
			Statement.functionBody(p, FUNCTION_MEMBER_RECOVERY);
			p.close(open, TreeKind.MEMB_DESTRUCTOR);
		}
	}

	static void parseOperator(TreeParser p, Modifiers.ModifierClose close) {

		ZSTokenType[] operatorStarts = {
				T_CAT,
				T_ADD,
				T_SUB,
				T_MUL,
				T_DIV,
				T_MOD,
				T_AND,
				T_OR,
				T_XOR,
				T_NOT,
				T_ADDASSIGN,
				T_SUBASSIGN,
				T_CATASSIGN,
				T_MULASSIGN,
				T_DIVASSIGN,
				T_MODASSIGN,
				T_ANDASSIGN,
				T_ORASSIGN,
				T_XORASSIGN,
				T_INCREMENT,
				T_DECREMENT,
				T_DOT2,
				T_SHL,
				T_SHR,
				T_USHR,
				T_SHLASSIGN,
				T_SHRASSIGN,
				T_USHRASSIGN,
				T_EQUAL2,
				K_IN
		};
		if (p.atAny(operatorStarts)) {
			TreeParser.MarkOpened open = p.openBefore(close.close());
			p.expect(operatorStarts);
			p.whitespace();
			FunctionHeader.parse(p, FUNCTION_MEMBER_RECOVERY);
			Statement.functionBody(p, FUNCTION_MEMBER_RECOVERY);

			p.close(open, TreeKind.MEMB_OPERATOR);
		}
	}

	static void parseCaster(TreeParser p, Modifiers.ModifierClose close) {
		if (p.atAny(K_AS)) {
			TreeParser.MarkOpened open = p.openBefore(close.close());
			p.expect(K_AS);
			p.whitespace();
			Type.parse(p, FUNCTION_MEMBER_RECOVERY);
			Statement.functionBody(p, FUNCTION_MEMBER_RECOVERY);
			p.close(open, TreeKind.MEMB_CASTER);
		}
	}
}
