package org.openzen.zenscript.parser;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.expression.ParsedExpressionString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EscapableBracketParser implements BracketExpressionParser {
	public static final EscapableBracketParser INSTANCE = new EscapableBracketParser();

	@Override
	public CompilableExpression parse(CodePosition position, ZSTokenParser tokens) throws ParseException {
		StringBuilder string = new StringBuilder();

		//This list will contain the BEP calls
		//If this is only a normal BEP, then it will contain exactly one String ParsedExpression.
		final List<CompilableExpression> parts = new ArrayList<>();

		while (tokens.optional(ZSTokenType.T_GREATER) == null) {
			ZSTokenType peekType = tokens.peek().getType();
			if(peekType == ZSTokenType.EOF) {
				throw new ParseException(position, "Reached EOF, BEP is missing a closing >");
			}
			if(tokens.getLastWhitespace().contains("\n")) {
				throw new ParseException(position, "BEPs cannot contain new lines!");
			}
			ZSToken next = tokens.next();

			if (next.type != ZSTokenType.T_DOLLAR) {
				string.append(next.content);
				string.append(tokens.getLastWhitespace());
				continue;
			}

			//We found a $, now check that it has a { directly after it.
			final String ws = tokens.getLastWhitespace();
			if (!ws.isEmpty()) {
				//$  {..} is not ${..} so we print it as literal
				string.append(next.content).append(ws);
				continue;
			}

			next = tokens.next();
			//Now we check if it is a {
			if (next.type == ZSTokenType.T_AOPEN) {
				if (string.length() != 0) {
					parts.add(new ParsedExpressionString(position, string.toString(), false));
					string = new StringBuilder();
				}
				parts.add(ParsedExpression.parse(tokens));
				tokens.required(ZSTokenType.T_ACLOSE, "} expected.");
				string.append(tokens.getLastWhitespace());
			} else {
				//No { after the $, so we treat them both as literal
				string.append("$").append(ws); //Technically, the whitespace here is empty, but let's be sure
				string.append(next.content).append(tokens.getLastWhitespace());
			}

		}

		if (string.length() != 0) {
			parts.add(new ParsedExpressionString(position, string.toString(), false));
		}

		return new EscapableBracketExpression(position, parts);
	}

	private static class EscapableBracketExpression implements CompilableExpression {
		private final CodePosition position;
		private final List<CompilableExpression> parts;

		public EscapableBracketExpression(CodePosition position, List<CompilableExpression> parts) {
			this.position = position;
			this.parts = parts;
		}

		@Override
		public CodePosition getPosition() {
			return position;
		}

		@Override
		public CompilingExpression compile(ExpressionCompiler compiler) {
			List<CompilingExpression> parts = this.parts.stream()
					.map(part -> part.compile(compiler))
					.collect(Collectors.toList());
			return new Compiling(compiler, position, parts);
		}
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final List<CompilingExpression> parts;

		public Compiling(ExpressionCompiler compiler, CodePosition position, List<CompilingExpression> parts) {
			super(compiler, position);

			this.parts = parts;
		}

		@Override
		public Expression eval() {
			ResolvedType string = compiler.resolve(BasicTypeID.STRING);
			CastedEval asString = CastedEval.implicit(compiler, position, BasicTypeID.STRING);
			InstanceCallable concat = string.findOperator(OperatorType.ADD)
					.orElseThrow(() -> new RuntimeException("String always has an add operator"));

			Expression result = parts.get(0).cast(asString).value;
			for (int i = 1; i < parts.size(); i++) {
				result = concat.cast(compiler, position, asString, result, TypeID.NONE, parts.get(i)).value;
			}
			return result;
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			for (CompilingExpression part : parts) {
				part.collect(collector);
			}
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			for (CompilingExpression part : parts) {
				part.linkVariables(linker);
			}
		}
	}
}
