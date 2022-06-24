package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParsedCallArguments {
	public static final ParsedCallArguments NONE = new ParsedCallArguments(Collections.emptyList());
	public final List<CompilableExpression> arguments;

	public ParsedCallArguments(List<CompilableExpression> arguments) {
		this.arguments = arguments;
	}

	public static ParsedCallArguments parse(ZSTokenParser tokens) throws ParseException {
		tokens.required(ZSTokenType.T_BROPEN, "( expected");

		List<CompilableExpression> arguments = new ArrayList<>();
		try {
			if (tokens.optional(ZSTokenType.T_BRCLOSE) == null) {
				do {
					arguments.add(ParsedExpression.parse(tokens));
				} while (tokens.optional(ZSTokenType.T_COMMA) != null);
				tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
			}
		} catch (ParseException ex) {
			tokens.logError(ex);
			tokens.recoverUntilOnToken(ZSTokenType.T_BRCLOSE);
		}

		return new ParsedCallArguments(arguments);
	}

	public static ParsedCallArguments parseForAnnotation(ZSTokenParser tokens) throws ParseException {
		List<CompilableExpression> arguments = new ArrayList<>();
		if (tokens.isNext(ZSTokenType.T_BROPEN)) {
			tokens.required(ZSTokenType.T_BROPEN, "( expected");
			try {
				if (tokens.optional(ZSTokenType.T_BRCLOSE) == null) {
					do {
						arguments.add(ParsedExpression.parse(tokens));
					} while (tokens.optional(ZSTokenType.T_COMMA) != null);
					tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
				}
			} catch (ParseException ex) {
				tokens.logError(ex);
				tokens.recoverUntilOnToken(ZSTokenType.T_BRCLOSE);
			}
		}

		return new ParsedCallArguments(arguments);
	}

	public CompilingExpression[] compile(ExpressionCompiler compiler) {
		return arguments.stream().map(arg -> arg.compile(compiler)).toArray(CompilingExpression[]::new);
	}
}
