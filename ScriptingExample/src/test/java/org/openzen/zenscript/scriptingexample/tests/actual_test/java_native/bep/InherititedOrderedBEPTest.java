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

public class InherititedOrderedBEPTest extends ZenCodeTest {

	@Override
	public BracketExpressionParser getBEP() {
		return new Parser();
	}

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(BracketHandlers.class);
		requiredClasses.add(Car.class);
		return requiredClasses;
	}

	@Test
	void works() {
		ScriptBuilder.create()
				.add("val array = [")
				.add(" <car>,")
				.add(" <electric>")
				.add(" ];")
				.add("println(array.length);")

				.add("val second = [")
				.add(" <electric>,")
				.add(" <car>")
				.add("];")
				.add("println(second.length);")
				.execute(this);

		logger.printlnOutputs()
				.assertLinesInOrder("2", "2");
	}


	@ZenCodeType.Name("test_module.Car")
	public static class Car {

	}

	static class ElectricCar extends Car {

	}

	@ZenCodeType.Name("test_module.BracketHandlers")
	public static final class BracketHandlers {

		@ZenCodeType.Method
		public static Car parseCar(String value) {
			if ("car".equals(value)) {
				return new Car();
			}
			if ("electric".equals(value)) {
				return new ElectricCar();
			}
			return null;
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
			return new ParsedExpressionCall(
					position,
					new ParsedExpressionMember(
							position,
							new ParsedTypeExpression(position, new ParsedNamedType(position, Arrays.asList(
									new ParsedNamedType.ParsedNamePart("test_module", null),
									new ParsedNamedType.ParsedNamePart("BracketHandlers", null)
							))),
							"parseCar",
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
