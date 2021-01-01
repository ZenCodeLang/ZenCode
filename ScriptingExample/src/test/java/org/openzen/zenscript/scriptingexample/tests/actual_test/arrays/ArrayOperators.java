package org.openzen.zenscript.scriptingexample.tests.actual_test.arrays;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class ArrayOperators extends ZenCodeTest {
	@Test
	public void containsReturnsTrueForMatch() {
		ScriptBuilder.create()
				.add("var array = ['a', 'b', 'c'] as string[];")
				.add("println(('a' in array) as string);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "true");
	}

	@Test
	public void containsReturnsFalseForNonMatch() {
		ScriptBuilder.create()
				.add("var array = ['a', 'b', 'c'] as string[];")
				.add("println(('d' in array) as string);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "false");
	}

	@Test
	public void canCastToList() {
		ScriptBuilder.create()
				.add("var array = ['a', 'b', 'c'] as string[];")
				.add("var list = array as stdlib.List<string>;")
				.add("var listImplicit as stdlib.List<string>;")
				.add("listImplicit = array;")
				.add("println(list.length);")
				.add("println(listImplicit.length);")
				.execute(this);

		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "3");
		logger.assertPrintOutput(1, "3");
	}

	@Test
	public void canCastFromList() {
		ScriptBuilder.create()
				.add("var list = new stdlib.List<string>();")
				.add("list.add('a');")
				.add("list.add('b');")
				.add("list.add('c');")
				.add("var array = list as string[];")
				.add("var arrayImplicit as string[];")
				.add("arrayImplicit = list;")
				.add("println(array.length);")
				.add("println(arrayImplicit.length);")
				.execute(this);

		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "3");
		logger.assertPrintOutput(1, "3");
	}
}
