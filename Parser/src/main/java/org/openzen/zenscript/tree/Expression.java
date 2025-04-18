package org.openzen.zenscript.tree;

import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenSet;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.lexer.PositionedToken;

import java.util.ArrayList;
import java.util.List;

import static org.openzen.zenscript.lexer.ZSTokenType.*;

class Expression {

	static final ZSTokenSet EXPRESSION_RECOVERY_SET = ZSTokenSet.of(T_SEMICOLON);

	static TreeParser.MarkClosed parse(TreeParser p) {
		return parse(p, ParseOptions.EMPTY);
	}

	static TreeParser.MarkClosed parse(TreeParser p, ParseOptions options) {
		return parseAssign(p, options);
	}

	static TreeParser.MarkClosed parseAssign(TreeParser p, ParseOptions options) {
		TreeParser.MarkClosed close = parseConditional(p, options);
		ZSTokenType[] tokens = new ZSTokenType[]{T_ASSIGN, T_ADDASSIGN, T_SUBASSIGN, T_CATASSIGN, T_MULASSIGN, T_DIVASSIGN, T_MODASSIGN, T_ORASSIGN, T_ANDASSIGN, T_XORASSIGN, T_SHLASSIGN, T_SHRASSIGN, T_USHRASSIGN};
		if (p.atAny(tokens)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			switch (p.nth(0)) {
				case T_ASSIGN:
					p.expect(T_ASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_ASSIGN);
				case T_ADDASSIGN:
					p.expect(T_ADDASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_ADD_ASSIGN);
				case T_SUBASSIGN:
					p.expect(T_SUBASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_SUB_ASSIGN);
				case T_CATASSIGN:
					p.expect(T_CATASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_CAT_ASSIGN);
				case T_MULASSIGN:
					p.expect(T_MULASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_MUL_ASSIGN);
				case T_DIVASSIGN:
					p.expect(T_DIVASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_DIV_ASSIGN);
				case T_MODASSIGN:
					p.expect(T_MODASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_MOD_ASSIGN);
				case T_ORASSIGN:
					p.expect(T_ORASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_OR_ASSIGN);
				case T_ANDASSIGN:
					p.expect(T_ANDASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_AND_ASSIGN);
				case T_XORASSIGN:
					p.expect(T_XORASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_XOR_ASSIGN);
				case T_SHLASSIGN:
					p.expect(T_SHLASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_SHL_ASSIGN);
				case T_SHRASSIGN:
					p.expect(T_SHRASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_SHR_ASSIGN);
				case T_USHRASSIGN:
					p.expect(T_USHRASSIGN);
					p.whitespace();
					parseAssign(p, options);
					return p.close(open, TreeKind.EXPR_USHR_ASSIGN);
			}
		}
		return close;
	}

	static TreeParser.MarkClosed parseConditional(TreeParser p, ParseOptions options) {
		TreeParser.MarkClosed close = parseOrOr(p, options);

		if (p.at(T_QUEST)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			p.expect(T_QUEST);
			p.whitespace();
			parseOrOr(p, options);
			p.expect(T_COLON);
			p.whitespace();
			parseConditional(p, options);
			return p.close(open, TreeKind.EXPR_CONDITIONAL);
		}
		return close;
	}

	static TreeParser.MarkClosed parseOrOr(TreeParser p, ParseOptions options) {
		TreeParser.MarkClosed close = parseAndAnd(p, options);

		while (p.at(T_OROR)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			p.expect(T_OROR);
			p.whitespace();
			parseAndAnd(p, options);
			close = p.close(open, TreeKind.EXPR_OR_OR);
		}

		while (p.at(T_COALESCE)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			p.expect(T_COALESCE);
			p.whitespace();
			parseAndAnd(p, options);
			close = p.close(open, TreeKind.EXPR_COALESCE);
		}

		return close;
	}

	static TreeParser.MarkClosed parseAndAnd(TreeParser p, ParseOptions options) {
		TreeParser.MarkClosed close = parseOr(p, options);

		while (p.at(T_ANDAND)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			p.expect(T_ANDAND);
			p.whitespace();
			parseOr(p, options);
			close = p.close(open, TreeKind.EXPR_AND_AND);
		}
		return close;
	}

	static TreeParser.MarkClosed parseOr(TreeParser p, ParseOptions options) {
		TreeParser.MarkClosed close = parseXor(p, options);

		while (p.at(T_OR)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			p.expect(T_OR);
			p.whitespace();
			parseXor(p, options);
			close = p.close(open, TreeKind.EXPR_OR);
		}
		return close;
	}

	static TreeParser.MarkClosed parseXor(TreeParser p, ParseOptions options) {
		TreeParser.MarkClosed close = parseAnd(p, options);

		while (p.at(T_XOR)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			p.expect(T_XOR);
			p.whitespace();
			parseAnd(p, options);
			close = p.close(open, TreeKind.EXPR_XOR);
		}
		return close;
	}

	static TreeParser.MarkClosed parseAnd(TreeParser p, ParseOptions options) {
		TreeParser.MarkClosed close = parseCompare(p, options);

		while (p.at(T_AND)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			p.expect(T_AND);
			p.whitespace();
			parseCompare(p, options);
			close = p.close(open, TreeKind.EXPR_AND);
		}
		return close;
	}

	static TreeParser.MarkClosed parseCompare(TreeParser p, ParseOptions options) {
		TreeParser.MarkClosed close = parseShift(p, options);
		ZSTokenType[] tokens = new ZSTokenType[]{T_EQUAL2, T_EQUAL3, T_NOTEQUAL, T_NOTEQUAL2, T_LESS, T_LESSEQ, T_GREATER, T_GREATEREQ, K_IN, K_IS, T_NOT};
		if (p.atAny(tokens)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			switch (p.nth(0)) {
				case T_EQUAL2:
					p.expect(T_EQUAL2);
					p.whitespace();
					parseShift(p, options);
					close = p.close(open, TreeKind.EXPR_EQ);
					p.whitespace();
					return close;
				case T_EQUAL3:
					p.expect(T_EQUAL3);
					p.whitespace();
					parseShift(p, options);
					close = p.close(open, TreeKind.EXPR_SAME);
					p.whitespace();
					return close;
				case T_NOTEQUAL:
					p.expect(T_NOTEQUAL);
					p.whitespace();
					parseShift(p, options);
					close = p.close(open, TreeKind.EXPR_NE);
					p.whitespace();
					return close;
				case T_NOTEQUAL2:
					p.expect(T_NOTEQUAL2);
					p.whitespace();
					parseShift(p, options);
					close = p.close(open, TreeKind.EXPR_NOT_SAME);
					p.whitespace();
					return close;
				case T_LESS:
					p.expect(T_LESS);
					p.whitespace();
					parseShift(p, options);
					close = p.close(open, TreeKind.EXPR_LT);
					p.whitespace();
					return close;
				case T_LESSEQ:
					p.expect(T_LESSEQ);
					p.whitespace();
					parseShift(p, options);
					close = p.close(open, TreeKind.EXPR_LE);
					p.whitespace();
					return close;
				case T_GREATER:
					p.expect(T_GREATER);
					p.whitespace();
					parseShift(p, options);
					close = p.close(open, TreeKind.EXPR_GT);
					p.whitespace();
					return close;
				case T_GREATEREQ:
					p.expect(T_GREATEREQ);
					p.whitespace();
					parseShift(p, options);
					close = p.close(open, TreeKind.EXPR_GE);
					p.whitespace();
					return close;
				case K_IN:
					p.expect(K_IN);
					p.whitespace();
					parseShift(p, options);
					close = p.close(open, TreeKind.EXPR_CONTAINS);
					p.whitespace();
					return close;
				case K_IS:
					p.expect(K_IS);
					p.whitespace();
					Type.parse(p);
					close = p.close(open, TreeKind.EXPR_IS);
					p.whitespace();
					return close;
				case T_NOT:
					p.expect(T_NOT);
					p.whitespace();
					if (p.eat(K_IN)) {
						p.whitespace();
						parseShift(p, options);
						close = p.close(open, TreeKind.EXPR_NOT_IN);
						p.whitespace();
						return close;
					} else if (p.eat(K_IS)) {
						p.whitespace();
						Type.parse(p);
						close = p.close(open, TreeKind.EXPR_NOT_IS);
						p.whitespace();
						return close;
					} else {
						p.error("Expected 'in' or 'is' after '!'");
					}
			}
		}
		return close;
	}

	static TreeParser.MarkClosed parseShift(TreeParser p, ParseOptions options) {

		TreeParser.MarkClosed close = parseAdd(p, options);
		ZSTokenType[] tokens = new ZSTokenType[]{T_SHL, T_SHR, T_USHR};
		while (p.atAny(tokens)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			if (p.eat(T_SHL)) {
				p.whitespace();
				parseAdd(p, options);
				close = p.close(open, TreeKind.EXPR_SHL);
				p.whitespace();
			} else if (p.eat(T_SHR)) {
				p.whitespace();
				parseAdd(p, options);
				close = p.close(open, TreeKind.EXPR_SHR);
				p.whitespace();
			} else if (p.eat(T_USHR)) {
				p.whitespace();
				parseAdd(p, options);
				close = p.close(open, TreeKind.EXPR_USHR);
				p.whitespace();
			}
		}
		return close;
	}

	static TreeParser.MarkClosed parseAdd(TreeParser p, ParseOptions options) {

		TreeParser.MarkClosed close = parseMul(p, options);
		while (true) {
			p.replace(token -> {
				List<PositionedToken<ZSTokenType, ZSToken>> tokens = new ArrayList<>();
				if ((token.getType() == T_INT || token.getType() == T_PREFIXED_INT) && token.getContent().startsWith("-")) {
					tokens.add(new PositionedToken<>(token.position().withLength(1), T_SUB.flyweight));
					tokens.add(new PositionedToken<>(token.position().subPosition(1), new ZSToken(token.getType(), token.getContent().substring(1))));
				}
				return tokens;
			});
			TreeParser.MarkOpened open = null;
			if (p.at(T_ADD)) {
				open = p.openBefore(close);
				p.expect(T_ADD);
				p.whitespace();
				parseMul(p, options);
				close = p.close(open, TreeKind.EXPR_ADD);
				p.whitespace();
			} else if (p.at(T_SUB)) {
				open = p.openBefore(close);
				p.expect(T_SUB);
				p.whitespace();
				parseMul(p, options);
				close = p.close(open, TreeKind.EXPR_SUB);
				p.whitespace();
			} else if (p.at(T_CAT)) {
				open = p.openBefore(close);
				p.expect(T_CAT);
				p.whitespace();
				parseMul(p, options);
				close = p.close(open, TreeKind.EXPR_CAT);
				p.whitespace();
			} else {
				break;
			}
		}
		return close;
	}

	static TreeParser.MarkClosed parseMul(TreeParser p, ParseOptions options) {

		TreeParser.MarkClosed close = parseUnary(p, options);
		ZSTokenType[] tokens = new ZSTokenType[]{T_MUL, T_DIV, T_MOD};

		while (p.atAny(tokens)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			if (p.eat(T_MUL)) {
				p.whitespace();
				parseUnary(p, options);
				close = p.close(open, TreeKind.EXPR_MUL);
			} else if (p.eat(T_DIV)) {
				p.whitespace();
				parseUnary(p, options);
				close = p.close(open, TreeKind.EXPR_DIV);
			} else if (p.eat(T_MOD)) {
				p.whitespace();
				parseUnary(p, options);
				close = p.close(open, TreeKind.EXPR_MOD);
			}
		}
		return close;
	}

	static TreeParser.MarkClosed parseUnary(TreeParser p, ParseOptions options) {

		ZSTokenType[] tokens = new ZSTokenType[]{T_NOT, T_SUB, T_CAT, T_INCREMENT, T_DECREMENT, K_TRY};

		if (p.atAny(tokens)) {
			TreeParser.MarkOpened unaryOpen = null;
			TreeParser.MarkClosed close = null;
			switch (p.nth(0)) {
				case T_NOT:
					unaryOpen = p.open();
					p.expect(T_NOT);
					p.whitespace();
					parseUnary(p, options);
					close = p.close(unaryOpen, TreeKind.EXPR_NOT);
					p.whitespace();
					return close;
				case T_SUB:
					unaryOpen = p.open();
					p.expect(T_SUB);
					p.whitespace();
					parseUnary(p, options);
					close = p.close(unaryOpen, TreeKind.EXPR_NEG);
					p.whitespace();
					return close;
				case T_CAT:
					unaryOpen = p.open();
					p.expect(T_CAT);
					p.whitespace();
					parseUnary(p, options);
					close = p.close(unaryOpen, TreeKind.EXPR_INVERT);
					p.whitespace();
					return close;
				case T_INCREMENT:
					unaryOpen = p.open();
					p.expect(T_INCREMENT);
					p.whitespace();
					parseUnary(p, options);
					close = p.close(unaryOpen, TreeKind.EXPR_INCREMENT);
					p.whitespace();
					return close;
				case T_DECREMENT:
					unaryOpen = p.open();
					p.expect(T_DECREMENT);
					p.whitespace();
					parseUnary(p, options);
					close = p.close(unaryOpen, TreeKind.EXPR_DECREMENT);
					p.whitespace();
					return close;
				case K_TRY:
					unaryOpen = p.open();
					p.expect(K_TRY);
					p.whitespace();
					if (p.eat(T_QUEST)) {
						p.whitespace();
						parseUnary(p, options);
						close = p.close(unaryOpen, TreeKind.EXPR_TRY_CONVERT);
						p.whitespace();
						return close;
					} else if (p.eat(T_NOT)) {
						p.whitespace();
						parseUnary(p, options);
						close = p.close(unaryOpen, TreeKind.EXPR_TRY_RETHROW);
						p.whitespace();
						return close;
					} else {
						p.error("Expected '?' or '!' after 'try'");
					}
			}
		}

		return parsePostfix(p, options);
	}

	static TreeParser.MarkClosed parsePostfix(TreeParser p, ParseOptions options) {
		TreeParser.MarkClosed markClosed = parsePrimary(p, options);

		ZSTokenType[] postfixTokens = new ZSTokenType[]{T_DOT, T_DOT2, T_SQOPEN, T_BROPEN, K_AS, T_INCREMENT, T_DECREMENT};

		while ((p.atAny(postfixTokens) || (options.allowLambdas && p.at(T_LAMBDA))) && !p.eof()) {
			TreeParser.MarkOpened postfixOpen = null;
			switch (p.nth(0)) {
				case T_DOT:
					postfixOpen = p.openBefore(markClosed);
					p.expect(T_DOT);
					p.whitespace();
					if (p.at(T_IDENTIFIER)) {
						p.name();
						Generics.parseTypeArguments(p);
						markClosed = p.close(postfixOpen, TreeKind.EXPR_MEMBER);
						break;
					} else if (p.eat(T_DOLLAR)) {
						p.whitespace();
						markClosed = p.close(postfixOpen, TreeKind.EXPR_OUTER);
						break;
					} else {
						if (p.eat(T_STRING_SQ) || p.eat(T_STRING_DQ)) {
							p.whitespace();
							markClosed = p.close(postfixOpen, TreeKind.EXPR_MEMBER);
						} else {
							markClosed = p.close(postfixOpen, TreeKind.ERROR);
						}
						break;
					}
				case T_DOT2:
					postfixOpen = p.openBefore(markClosed);
					p.expect(T_DOT2);
					p.whitespace();
					parse(p);
					markClosed = p.close(postfixOpen, TreeKind.EXPR_RANGE);
					break;
				case T_SQOPEN:
					postfixOpen = p.openBefore(markClosed);
					p.expect(T_SQOPEN);
					do {
						p.whitespace();
						TreeParser.MarkOpened indexOpen = p.open();
						parse(p);
						p.close(indexOpen, TreeKind.INDEX_KEY);
					} while (p.eat(T_COMMA));
					p.expect(T_SQCLOSE);
					p.whitespace();
					markClosed = p.close(postfixOpen, TreeKind.EXPR_INDEX);
					break;
				case T_BROPEN:
					postfixOpen = p.openBefore(markClosed);
					CallArguments.parse(p);
					markClosed = p.close(postfixOpen, TreeKind.EXPR_CALL);
					break;
				case K_AS:
					postfixOpen = p.openBefore(markClosed);
					p.expect(K_AS);
					p.whitespace();
					// eat the quest if it is there
					p.eat(T_QUEST);
					p.whitespace();
					Type.parse(p);
					markClosed = p.close(postfixOpen, TreeKind.EXPR_CAST);
					break;
				case T_INCREMENT:
					postfixOpen = p.openBefore(markClosed);
					p.expect(T_INCREMENT);
					p.whitespace();
					markClosed = p.close(postfixOpen, TreeKind.EXPR_INCREMENT);
					break;
				case T_DECREMENT:
					postfixOpen = p.openBefore(markClosed);
					p.expect(T_DECREMENT);
					p.whitespace();
					markClosed = p.close(postfixOpen, TreeKind.EXPR_DECREMENT);
					break;
				case T_LAMBDA:
					if (!options.allowLambdas) {
						break;
					}
					//TODO this should check if a lambda is allowed? or we can check that later
					postfixOpen = p.openBefore(markClosed);
					p.expect(T_LAMBDA);
					p.whitespace();
					Statement.lambdaBody(p, true);
					markClosed = p.close(postfixOpen, TreeKind.EXPR_FUNCTION);
					break;
			}
		}
		return markClosed;
	}

	static TreeParser.MarkClosed parsePrimary(TreeParser p, ParseOptions options) {
		// bracket < intentionally left out
		ZSTokenType[] primaryTokens = new ZSTokenType[]{T_INT, T_PREFIXED_INT, T_FLOAT, T_STRING_DQ, T_STRING_SQ, T_IDENTIFIER, T_LOCAL_IDENTIFIER, K_THIS, K_SUPER, T_DOLLAR, T_SQOPEN, T_AOPEN, K_TRUE, K_FALSE, K_NULL, T_BROPEN, K_NEW, K_THROW, K_PANIC, K_MATCH, T_LESS};

		if (p.atAny(primaryTokens)) {
			TreeParser.MarkOpened primaryOpen = null;
			TreeParser.MarkClosed close = null;
			switch (p.nth(0)) {
				case T_INT:
					primaryOpen = p.open();
					p.expect(T_INT);
					p.whitespace();
					close = p.close(primaryOpen, TreeKind.EXPR_INT);
					p.whitespace();
					return close;
				case T_PREFIXED_INT:
					primaryOpen = p.open();
					p.expect(T_PREFIXED_INT);
					p.whitespace();
					close = p.close(primaryOpen, TreeKind.EXPR_PREFIXED_INT);
					p.whitespace();
					return close;
				case T_FLOAT:
					primaryOpen = p.open();
					p.expect(T_FLOAT);
					p.whitespace();
					close = p.close(primaryOpen, TreeKind.EXPR_FLOAT);
					p.whitespace();
					return close;
				case T_STRING_DQ:
				case T_STRING_SQ:
					primaryOpen = p.open();
					p.expect(T_STRING_DQ, T_STRING_SQ);
					p.whitespace();
					close = p.close(primaryOpen, TreeKind.EXPR_STRING);
					p.whitespace();
					return close;
				case T_IDENTIFIER:
					primaryOpen = p.open();
					p.name();
					Generics.parseTypeArguments(p);
					close = p.close(primaryOpen, TreeKind.EXPR_VARIABLE);
					p.whitespace();
					return close;
				case T_LOCAL_IDENTIFIER:
					primaryOpen = p.open();
					p.expect(T_LOCAL_IDENTIFIER);
					p.whitespace();
					close = p.close(primaryOpen, TreeKind.EXPR_LOCAL_VARIABLE);
					p.whitespace();
					return close;
				case K_THIS:
					primaryOpen = p.open();
					p.expect(K_THIS);
					p.whitespace();
					close = p.close(primaryOpen, TreeKind.EXPR_THIS);
					p.whitespace();
					return close;
				case K_SUPER:
					primaryOpen = p.open();
					p.expect(K_SUPER);
					p.whitespace();
					close = p.close(primaryOpen, TreeKind.EXPR_SUPER);
					p.whitespace();
					return close;
				case T_DOLLAR:
					primaryOpen = p.open();
					p.expect(T_DOLLAR);
					p.whitespace();
					close = p.close(primaryOpen, TreeKind.EXPR_OUTER);
					p.whitespace();
					return close;
				case T_SQOPEN:
					primaryOpen = p.open();
					p.expect(T_SQOPEN);
					p.whitespace();
					if (!p.at(T_SQCLOSE)) {
						while (!p.eat(T_SQCLOSE) && !p.eof()) {
							p.whitespace();
							parse(p);
							if (!p.eat(T_COMMA)) {
								p.expect(T_SQCLOSE);
								p.whitespace();
								break;
							}
							p.whitespace();
						}
					} else {
						p.expect(T_SQCLOSE);
						p.whitespace();
					}

					close = p.close(primaryOpen, TreeKind.EXPR_ARRAY);
					p.whitespace();
					return close;
				case T_AOPEN:
					primaryOpen = p.open();
					p.expect(T_AOPEN);
					p.whitespace();
					while (!p.eat(T_ACLOSE) && !p.eof()) {
						TreeParser.MarkOpened keyOrValOpen = p.open();
						parse(p);
						if (p.eat(T_COLON)) {
							p.whitespace();
							p.close(keyOrValOpen, TreeKind.EXPR_MAP_KEY);
							TreeParser.MarkOpened valOpen = p.open();
							parse(p);
							p.close(valOpen, TreeKind.EXPR_MAP_VALUE);
						} else {
							p.close(keyOrValOpen, TreeKind.EXPR_MAP_VALUE);
						}
						if (!p.eat(T_COMMA)) {
							p.expect(T_ACLOSE);
							p.whitespace();
							break;
						}
						p.whitespace();
					}
					close = p.close(primaryOpen, TreeKind.EXPR_MAP);
					p.whitespace();
					return close;
				case K_TRUE:
				case K_FALSE:
					primaryOpen = p.open();
					p.expect(K_TRUE, K_FALSE);
					p.whitespace();
					close = p.close(primaryOpen, TreeKind.EXPR_BOOL);
					p.whitespace();
					return close;
				case K_NULL:
					primaryOpen = p.open();
					p.expect(K_NULL);
					p.whitespace();
					close = p.close(primaryOpen, TreeKind.EXPR_NULL);
					p.whitespace();
					return close;
				case T_BROPEN:
					primaryOpen = p.open();
					p.expect(T_BROPEN);
					do {
						p.whitespace();
						if (p.at(T_BRCLOSE)) {
							break;
						}
						parse(p);
					} while (p.eat(T_COMMA) && !p.eof());
					p.expect(T_BRCLOSE);
					p.whitespace();
					close = p.close(primaryOpen, TreeKind.EXPR_BRACKET);
					p.whitespace();
					return close;
				case K_NEW:
					primaryOpen = p.open();
					p.expect(K_NEW);
					p.whitespace();
					Type.parse(p);
					if (p.at(T_BROPEN)) {
						CallArguments.parse(p);
					}
					close = p.close(primaryOpen, TreeKind.EXPR_NEW);
					p.whitespace();
					return close;
				case K_THROW:
					primaryOpen = p.open();
					p.expect(K_THROW);
					p.whitespace();
					parse(p);
					close = p.close(primaryOpen, TreeKind.EXPR_THROW);
					p.whitespace();
					return close;
				case K_PANIC:
					primaryOpen = p.open();
					p.expect(K_PANIC);
					p.whitespace();
					parse(p);
					close = p.close(primaryOpen, TreeKind.EXPR_PANIC);
					p.whitespace();
					return close;
				case K_MATCH:

					primaryOpen = p.open();
					p.expect(K_MATCH);
					p.whitespace();

					parse(p);

					p.expect(T_AOPEN);
					p.whitespace();

					while (!p.at(T_ACLOSE)) {

						// parse the key
						TreeParser.MarkOpened keyOpen = p.open();
						// Eat K_DEFAULT if it is here, otherwise parse the key
						if (!p.eat(K_DEFAULT)) {
							parse(p, ParseOptions.NO_LAMBDAS);
						}
						p.whitespace();
						p.close(keyOpen, TreeKind.EXPR_MATCH_KEY);

						p.expect(T_LAMBDA);
						p.whitespace();
						// parse the value
						parse(p, options);


						if (!p.eat(T_COMMA)) {
							break;
						}
						p.whitespace();
					}
					p.expect(T_ACLOSE);
					close = p.close(primaryOpen, TreeKind.EXPR_MATCH);
					p.whitespace();
					return close;

				case T_LESS:
					primaryOpen = p.open();
					p.expect(T_LESS);
					p.expect(T_IDENTIFIER);
					p.expect(T_COLON);
					while (!p.atAny(T_LESS, T_GREATER) && !p.eof()) {
						p.eatAny();
					}
// TODO recover
					p.expect(T_GREATER);
					close = p.close(primaryOpen, TreeKind.EXPR_BEP);
					p.whitespace();
					return close;
			}
		}

		TreeParser subTree = p.subTree();

		TreeParser.MarkOpened typeOpen = subTree.open();
		if (Type.parse(subTree)) {
			TreeParser.MarkClosed close = subTree.close(typeOpen, TreeKind.EXPR_TYPE);
			p.mergeIn(subTree);
			p.whitespace();
			return close;
		}
//		p.advanceWithError("Expected type");
		return null;

//		p.advanceWithError("Expected primary expression, stuck on " + p.nth(0));
//		return null;
	}

	static class ParseOptions {
		public static final ParseOptions EMPTY = new ParseOptions(true);
		public static final ParseOptions NO_LAMBDAS = new ParseOptions(false);

		private final boolean allowLambdas;

		public ParseOptions(boolean allowLambdas) {
			this.allowLambdas = allowLambdas;
		}

		public boolean allowLambdas() {
			return allowLambdas;
		}
	}
}
