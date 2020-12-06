package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.expression.NewExpression;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.expression.ParsedCallArguments;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.expression.ParsedNewExpression;

import java.util.ArrayList;
import java.util.List;

public class ParsedEnumConstant {
	public final CodePosition position;
	public final String name;
	public final List<ParsedExpression> arguments;
	public final ParsedExpression value;
	private final EnumConstantMember compiled;

	public ParsedEnumConstant(CodePosition position, HighLevelDefinition definition, String name, int value, List<ParsedExpression> arguments, ParsedExpression expressionValue) {
		this.position = position;
		this.name = name;
		this.arguments = arguments;
		this.value = expressionValue;

		compiled = new EnumConstantMember(position, definition, name, value);
	}

	public static ParsedEnumConstant parse(ZSTokenParser tokens, EnumDefinition definition, int value) throws ParseException {
		CodePosition position = tokens.getPosition();
		ZSToken name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected");
		List<ParsedExpression> arguments = new ArrayList<>();
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

		ParsedExpression valueExpression = null;
		if (tokens.optional(ZSTokenType.T_ASSIGN) != null)
			valueExpression = ParsedExpression.parse(tokens);

		return new ParsedEnumConstant(position, definition, name.content, value, arguments, valueExpression);
	}

	public EnumConstantMember getCompiled() {
		return compiled;
	}

	public void compileCode(DefinitionTypeID type, ExpressionScope scope) throws CompileException {
		ParsedCallArguments arguments = new ParsedCallArguments(null, this.arguments);
		compiled.constructor = (NewExpression) ParsedNewExpression.compile(position, type, arguments, scope);

		if (value != null)
			compiled.value = value.compile(scope).eval();
	}
}
