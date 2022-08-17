package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;

public class SimpleBracketParser implements BracketExpressionParser {
	private final MethodSymbol method;

	public SimpleBracketParser(MethodSymbol method) {
		if (!method.getModifiers().isStatic())
			throw new IllegalArgumentException("Method must be static");
		if (method.getHeader().getNumberOfTypeParameters() > 0)
			throw new IllegalArgumentException("Method cannot have type parameters");

		this.method = method;
	}

	@Override
	public CompilableExpression parse(CodePosition position, ZSTokenParser tokens) throws ParseException {
		StringBuilder string = new StringBuilder();
		while (tokens.optional(ZSTokenType.T_GREATER) == null) {
			ZSTokenType peekType = tokens.peek().getType();
			if(peekType == ZSTokenType.EOF) {
				throw new ParseException(position, "Reached EOF, BEP is missing a closing >");
			}
			if(tokens.getLastWhitespace().contains("\n")) {
				throw new ParseException(position, "BEPs cannot contain new lines!");
			}
			string.append(tokens.next().content);
			string.append(tokens.getLastWhitespace());
		}
		return new StaticMethodCallExpression(position, string.toString());
	}

	private class StaticMethodCallExpression implements CompilableExpression {
		private final CodePosition position;
		private final String value;

		public StaticMethodCallExpression(CodePosition position, String value) {
			this.position = position;
			this.value = value;
		}

		@Override
		public CodePosition getPosition() {
			return position;
		}

		@Override
		public CompilingExpression compile(ExpressionCompiler compiler) {
			return compiler
					.at(position)
					.callStatic(new MethodInstance(method), new CallArguments(new ConstantStringExpression(position, value)))
					.wrap(compiler);
		}
	}
}
