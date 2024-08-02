package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.bep;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.EscapableBracketParser;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class EscapableBracketParserTests extends ZenCodeTest {
	@Override
	public BracketExpressionParser getBEP() {
		return new EscapableBracketParser();
	}

	@Test
	void canBeUsedWithoutEscaping() {
		ScriptBuilder.create()
				.add("var result = <some_string>;")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs()
				.assertLinesInOrder("some_string");
	}

	@Test
	void canBeUsedWithEscaping() {
		ScriptBuilder.create()
				.add("var escaped = 'world';")
				.add("var result = <hello_${escaped}_string>;")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs()
				.assertLinesInOrder("hello_world_string");
	}

	@Test
	void expressionsCanBeUsed() {
		ScriptBuilder.create()
				.add("var escaped = 7;")
				.add("var result = <hello_${escaped * 3}_string>;")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs()
				.assertLinesInOrder("hello_21_string");
	}

	@Test
	void multipleExpressionsCanBeUsed() {

		ScriptBuilder.create()
				.add("var greeting = 'Hello';")
				.add("var greeted = 'World';")
				.add("var result = <${greeting}_${greeted}.>;")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs()
				.assertLinesInOrder("Hello_World.");
	}
}
