package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.bep;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.expression.*;
import org.openzen.zenscript.parser.type.ParsedNamedType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BepThatUsesModuleTest extends ZenCodeTest {
	@Override
	public BracketExpressionParser getBEP() {
		return new Parser();
	}

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(BracketHandlers.class);
		return requiredClasses;
	}




	@Test
	void works() {
		ScriptBuilder.create()
				.add("var doubledValue = <1.0>;")
				.add("println(doubledValue);")
				.execute(this);

		logger.printlnOutputs()
				.assertLinesInOrder("2.0");
	}

	@ZenCodeType.Name("test_module.BracketHandlers")
	public static final class BracketHandlers {

		@ZenCodeType.Method
		public static double parsedAndDoubleTheValue(String value) {
			return 2 * Double.parseDouble(value);
		}
	}

	private static final class Parser implements BracketExpressionParser {
		@Override
		public CompilableExpression parse(CodePosition position, ZSTokenParser tokens) throws ParseException {
			StringBuilder stringBuilder = new StringBuilder();
			while (!tokens.isNext(ZSTokenType.T_GREATER)) {
				stringBuilder.append(tokens.next().getContent());
			}
			tokens.next();

			// test_module.BracketHandlers.parsedAndDoubleTheValue("<parsedValue>")
			return new ParsedExpressionCall(
					position,
					new ParsedExpressionMember(
							position,
							new ParsedTypeExpression(position, new ParsedNamedType(position, Arrays.asList(
									new ParsedNamedType.ParsedNamePart("test_module", null),
									new ParsedNamedType.ParsedNamePart("BracketHandlers", null)
							))),
							"parsedAndDoubleTheValue",
							null
					),
					new ParsedCallArguments(
							Collections.singletonList(
									new ParsedExpressionString(position, stringBuilder.toString(), false)
							)
					)
			);
		}
	}
}
