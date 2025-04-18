package org.openzen.zenscript.tree;

import org.openzen.zenscript.lexer.ZSTokenSet;
import org.openzen.zenscript.lexer.ZSTokenType;

import static org.openzen.zenscript.lexer.ZSTokenType.*;

class Statement {

	static final ZSTokenSet STATEMENT_STARTS = ZSTokenSet.of(T_AOPEN, K_RETURN, K_VAR, K_VAL, K_IF, K_FOR, K_DO, K_WHILE, K_LOCK, K_THROW, K_TRY, K_CONTINUE, K_BREAK, K_SWITCH);

	//TODO do we want to remove aopen from this?
	static final ZSTokenSet STATEMENT_RECOVERY = STATEMENT_STARTS.without(T_AOPEN);

	static final ZSTokenSet FOR_RECOVERY = STATEMENT_RECOVERY.and(K_IN, T_COMMA);

	static void parse(TreeParser p) {

		if (p.atAny(STATEMENT_STARTS)) {
			TreeParser.MarkOpened open;
			ZSTokenType statementStart = p.nth(0);
			switch (statementStart) {
				case T_AOPEN:
					block(p);
					return;
				case K_RETURN:
					open = p.open();
					p.expect(K_RETURN);
					p.whitespace();
					// if not at semicolon, parse an expression
					if (!p.at(T_SEMICOLON)) {
						if (Expression.parse(p) == null) {
							p.recover("expected expression", STATEMENT_RECOVERY);
						}
					}
					// finally eat the semicolon
					p.expect(T_SEMICOLON);
					p.whitespace();
					p.close(open, TreeKind.STMT_RETURN);
					return;
				case K_VAR:
					open = p.open();
					p.expect(K_VAR);
					p.whitespace();
					p.name(STATEMENT_RECOVERY);
					Type.parseDeclaration(p, STATEMENT_RECOVERY);
					if (p.eat(T_ASSIGN)) {
						p.whitespace();
						if (Expression.parse(p) == null) {
							p.recover("expected expression", STATEMENT_RECOVERY);
						}
					}
					p.expect(T_SEMICOLON);
					p.whitespace();
					p.close(open, TreeKind.STMT_VAR);
					return;
				case K_VAL:
					open = p.open();
					p.expect(K_VAL);
					p.whitespace();
					p.name(STATEMENT_RECOVERY);
					Type.parseDeclaration(p, STATEMENT_RECOVERY);
					if (p.eat(T_ASSIGN)) {
						p.whitespace();
						if (Expression.parse(p) == null) {
							p.recover("expected expression", STATEMENT_RECOVERY);
						}
					}
					p.expect(T_SEMICOLON);
					p.whitespace();
					p.close(open, TreeKind.STMT_VAL);
					return;
				case K_IF:
					open = p.open();
					p.expect(K_IF);
					p.whitespace();
					if (Expression.parse(p) != null) {
						parse(p);
						if (p.at(K_ELSE)) {
							TreeParser.MarkOpened elseOpen = p.open();
							p.expect(K_ELSE);
							p.whitespace();
							parse(p);
							p.close(elseOpen, TreeKind.STMT_ELSE);
						}
					} else {
						p.recover("expected expression", STATEMENT_RECOVERY);
					}
					p.close(open, TreeKind.STMT_IF);
					return;

				case K_FOR:
					open = p.open();
					p.expect(K_FOR);
					p.whitespace();
					TreeParser.MarkOpened forNames = p.open();
					p.name(FOR_RECOVERY);

					while (p.at(T_COMMA)) {
						p.expect(T_COMMA);
						p.whitespace();
						p.name(FOR_RECOVERY);
					}
					p.close(forNames, TreeKind.FOR_NAMES);

					p.expect(K_IN);
					p.whitespace();

					if (Expression.parse(p) != null) {
						parse(p);
					} else {
						p.recover("expected expression", STATEMENT_RECOVERY);
					}

					p.close(open, TreeKind.STMT_FOR);
					return;

				case K_DO:
					//TODO DO currently has no error recovery
					open = p.open();
					p.expect(K_DO);
					p.whitespace();
					if (p.at(T_COLON)) {
						TreeParser.MarkOpened labelOpen = p.open();
						p.expect(T_COLON);
						p.whitespace();
						p.name();
						p.close(labelOpen, TreeKind.LABEL);
					}

					parse(p);

					p.expect(K_WHILE);
					p.whitespace();
					Expression.parse(p);

					p.expect(T_SEMICOLON);
					p.whitespace();
					p.close(open, TreeKind.STMT_DO_WHILE);
					return;

				case K_WHILE:

					open = p.open();
					p.expect(K_WHILE);
					p.whitespace();
					if (p.at(T_COLON)) {
						TreeParser.MarkOpened labelOpen = p.open();
						p.expect(T_COLON);
						p.whitespace();
						p.name();
						p.close(labelOpen, TreeKind.LABEL);
					}
					if (Expression.parse(p) != null) {
						parse(p);
					} else {
						p.recover("expected expression", STATEMENT_RECOVERY);
					}

					p.close(open, TreeKind.STMT_WHILE);
					return;

				case K_LOCK:
					open = p.open();
					p.expect(K_LOCK);
					p.whitespace();
					if (Expression.parse(p) != null) {
						parse(p);
					} else {
						p.recover("expected expression", STATEMENT_RECOVERY);
					}

					p.close(open, TreeKind.STMT_LOCK);
					return;
				case K_THROW:
					open = p.open();
					p.expect(K_THROW);
					p.whitespace();
					if (Expression.parse(p) == null) {
						p.recover("expected expression", STATEMENT_RECOVERY);
					}
					p.expect(T_SEMICOLON);
					p.whitespace();
					p.close(open, TreeKind.STMT_THROW);
					return;


				case K_TRY:
					// TODO try doesn't have error recovery right now
					ZSTokenType nth = p.nth(1);
					if (nth == T_QUEST || nth == T_NOT) {
						// fall through to try! or try? expression
						break;
					}
					open = p.open();
					p.expect(K_TRY);
					p.whitespace();

					if (p.at(T_IDENTIFIER)) {
						p.name(STATEMENT_RECOVERY);
						p.expect(T_ASSIGN);
						p.whitespace();
						if (Expression.parse(p) == null) {
							p.recover("expected expression", STATEMENT_RECOVERY);
						}
					}

					parse(p);

					while (p.at(K_CATCH)) {
						p.expect(K_CATCH);
						p.whitespace();
						if (p.at(T_IDENTIFIER)) {
							p.name();
						}
						parse(p);
					}

					if (p.at(K_FINALLY)) {
						p.expect(K_FINALLY);
						p.whitespace();
						parse(p);
					}

					p.close(open, TreeKind.STMT_TRY);
					return;

				case K_CONTINUE:
					open = p.open();
					p.expect(K_CONTINUE);
					p.whitespace();
					if (p.at(T_IDENTIFIER)) {
						p.name();
					}
					p.expect(T_SEMICOLON);
					p.whitespace();
					p.close(open, TreeKind.STMT_CONTINUE);
					return;

				case K_BREAK:
					open = p.open();
					p.expect(K_BREAK);
					p.whitespace();
					if (p.at(T_IDENTIFIER)) {
						p.name();
					}
					p.expect(T_SEMICOLON);
					p.whitespace();
					p.close(open, TreeKind.STMT_BREAK);
					return;
				case K_SWITCH:

					open = p.open();
					p.expect(K_SWITCH);
					p.whitespace();
					if (p.at(T_COLON)) {
						p.expect(T_COLON);
						p.whitespace();
						p.name();
					}

					if (Expression.parse(p) == null) {
						p.recover("expected expression", STATEMENT_RECOVERY);
					}

					if (p.at(T_AOPEN)) {
						TreeParser.MarkOpened casesOpen = p.open();
						p.expect(T_AOPEN);
						p.whitespace();
						// If we are at a case, lets open it up here
						TreeParser.MarkOpened caseOpen = null;
						while (!p.at(T_ACLOSE)) {
							if (p.at(K_CASE)) {
								if (caseOpen != null) {
									p.close(caseOpen, TreeKind.SWITCH_CASE);
								}
								caseOpen = p.open();
								p.expect(K_CASE);
								p.whitespace();
								Expression.parse(p);
								p.expect(T_COLON);
								p.whitespace();
							} else if (p.at(K_DEFAULT)) {
								if (caseOpen != null) {
									p.close(caseOpen, TreeKind.SWITCH_CASE);
								}
								caseOpen = p.open();
								p.expect(K_DEFAULT);
								p.whitespace();
								p.expect(T_COLON);
								p.whitespace();
							} else {
								parse(p);
							}
						}
						if (caseOpen != null) {
							p.close(caseOpen, TreeKind.SWITCH_CASE);
						}

						p.expect(T_ACLOSE);
						p.close(casesOpen, TreeKind.SWITCH_CASES);
					} else {
						p.recover("expected switch cases", STATEMENT_RECOVERY);
					}
					p.whitespace();
					p.close(open, TreeKind.STMT_SWITCH);
					return;
			}
		}
		//TODO Do we want to try expressions here?
		if (Expression.parse(p) == null) {
			p.recover("expected expression", STATEMENT_RECOVERY);
		}
		p.expect(T_SEMICOLON);
		p.whitespace();
		// TODO error recovery here
//		advanceWithError("Expected statement");
	}

	static void block(TreeParser p) {
		block(p, null);
	}

	static void block(TreeParser p, TreeParser.MarkClosed close) {
		if (p.at(T_AOPEN)) {
			TreeParser.MarkOpened open = p.openBefore(close);
			p.expect(T_AOPEN);
			p.whitespace();
			while (!p.at(T_ACLOSE) && !p.eof()) {
				parse(p);
			}
			//TODO is this recovery right?
			if (!p.eat(T_ACLOSE)) {
				p.recover("expected a closing brace", STATEMENT_RECOVERY);
			}
			p.close(open, TreeKind.BLOCK);
			p.whitespace();

		} else {
			p.error("expected a block");
		}
	}

	static void functionBody(TreeParser p, ZSTokenSet recovery) {
		if (p.at(T_LAMBDA)) {
			p.expect(T_LAMBDA);
			p.whitespace();
			lambdaBody(p, false);
		} else if (p.at(T_SEMICOLON)) {
			p.expect(T_SEMICOLON);
			p.whitespace();
		} else if (p.at(T_AOPEN)) {
			block(p);
		} else {
			p.recover("expected a function body", recovery);
		}
	}

	static void lambdaBody(TreeParser p, boolean inExpression) {
		//TODO error recovery
		if (p.at(T_AOPEN)) {
			TreeParser.MarkOpened open = p.open();
			p.expect(T_AOPEN);
			p.whitespace();
			while (!p.eat(T_ACLOSE) && !p.eof()) {
				parse(p);
			}
			p.whitespace();
			p.close(open, TreeKind.BLOCK);
		} else {
			TreeParser.MarkOpened open = p.open();
			Expression.parse(p);
			// TODO add validation here?
			if (!inExpression) {
				p.eat(T_SEMICOLON);
				p.whitespace();
			}
			p.close(open, TreeKind.LAMBDA_BODY);
		}
	}

}
