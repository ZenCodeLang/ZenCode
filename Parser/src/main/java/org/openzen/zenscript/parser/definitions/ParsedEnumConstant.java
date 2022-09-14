package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.expression.CallStaticExpression;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.expression.ParsedExpression;

import java.util.ArrayList;
import java.util.List;

public class ParsedEnumConstant {
	public final CodePosition position;
	public final String name;
	public final List<CompilableExpression> arguments;
	public final int ordinal;
	public final CompilableExpression value;

	public ParsedEnumConstant(CodePosition position, String name, int ordinal, List<CompilableExpression> arguments, CompilableExpression expressionValue) {
		this.position = position;
		this.name = name;
		this.arguments = arguments;
		this.ordinal = ordinal;
		this.value = expressionValue;
	}

	public static ParsedEnumConstant parse(ZSTokenParser tokens, int value) throws ParseException {
		CodePosition position = tokens.getPosition();
		ZSToken name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected");
		List<CompilableExpression> arguments = new ArrayList<>();
		if (tokens.optional(ZSTokenType.T_BROPEN) != null) {
			try {
				do {
					arguments.add(ParsedExpression.parse(tokens));
				} while (tokens.optional(ZSTokenType.T_COMMA) != null);
				tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
			} catch (ParseException ex) {
				tokens.logError(ex);
				tokens.recoverUntilOnToken(ZSTokenType.T_BRCLOSE);
			}
		}

		CompilableExpression valueExpression = null;
		if (tokens.optional(ZSTokenType.T_ASSIGN) != null)
			valueExpression = ParsedExpression.parse(tokens);

		return new ParsedEnumConstant(position, name.content, value, arguments, valueExpression);
	}

	public Compiling compile(EnumDefinition definition) {
		EnumConstantMember compiled = new EnumConstantMember(position, definition, name, ordinal);
		return new Compiling(compiled);
	}

	public class Compiling {
		public final EnumConstantMember compiled;

		public Compiling(EnumConstantMember compiled) {
			this.compiled = compiled;
		}

		public void compileCode(TypeID type, ExpressionCompiler compiler) {
			ResolvedType members = compiler.resolve(type);
			compiled.constructor = (CallStaticExpression) members.getConstructor().call(
					compiler,
					position,
					TypeID.NONE,
					arguments.stream().map(arg -> arg.compile(compiler)).toArray(CompilingExpression[]::new));

			if (value != null)
				compiled.value = value.compile(compiler).eval();
		}
	}
}
