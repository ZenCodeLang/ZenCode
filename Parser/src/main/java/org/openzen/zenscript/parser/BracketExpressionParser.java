package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.parser.expression.ParsedExpression;

public interface BracketExpressionParser {
	/**
	 * Parses the given bracket expression. Note that the "&lt;" token is already
	 * processed.
	 *
	 * @param position start of the expression (which is the location of the &gt; token)
	 * @param tokens   tokens to be parsed
	 * @return parsed expression
	 */
	ParsedExpression parse(CodePosition position, ZSTokenParser tokens) throws ParseException;
}
