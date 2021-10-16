package org.openzen.zenscript.scriptingexample.tests.actual_test.arrays;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ArrayOperatorTests extends ZenCodeTest {
	private static String createString(int length) {
		final StringBuilder stringBuilder = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			stringBuilder.append('A');
		}
		return stringBuilder.toString();
	}

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

	@Test
	public void indexMethodWorks() {
		ScriptBuilder.create()
				.add("var array = ['A', 'B', 'C'] as string[];")
				.add("var indexer as function(element as string) as string = (element as string) as string => element;")
				.add("var map = array.index<string>(indexer);")
				.add("for key, value in map println (key + ': ' + value);")
				.execute(this);

		logger.assertPrintOutputSize(3);
		logger.assertPrintOutput(0, "A: A");
		logger.assertPrintOutput(1, "B: B");
		logger.assertPrintOutput(2, "C: C");
	}

	@Test
	public void mapWorks() {
		ScriptBuilder.create()
				.add("var array = ['A', 'B', 'C'] as string[];")
				.add("var mapper as function(element as string) as string = (element as string) as string => element + element;")
				.add("var mapped = array.map<string>(mapper);")
				.add("for element in mapped println(element);")
				.execute(this);

		logger.assertPrintOutputSize(3);
		logger.assertPrintOutput(0, "AA");
		logger.assertPrintOutput(1, "BB");
		logger.assertPrintOutput(2, "CC");
	}

	@Test
	public void mapWithIndexWorks() {
		ScriptBuilder.create()
				.add("var array = ['A', 'B', 'C'] as string[];")
				.add("var mapper as function(index as usize, element as string) as string = (index as usize, element as string) as string => element + index + element;")
				.add("var mapped = array.map<string>(mapper);")
				.add("for element in mapped println(element);")
				.execute(this);

		logger.assertPrintOutputSize(3);
		logger.assertPrintOutput(0, "A0A");
		logger.assertPrintOutput(1, "B1B");
		logger.assertPrintOutput(2, "C2C");
	}
}
