package org.openzen.zenscript.scriptingexample.tests.actual_test.arrays;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.*;
import java.util.stream.Collectors;

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

	private static String createStringWithLength(int length) {
		final char[] chars = new char[length];
		Arrays.fill(chars, 'A');
		return new String(chars);
	}

	@Test
	public void sortedWithComparatorWorks() {
		final int length = 10;

		final List<String> array = new Random().ints(length, 0, 10)
				.mapToObj(ArrayOperators::createStringWithLength)
				.collect(Collectors.toList());

		String unsortedArrayString = array.stream()
				.map(string -> '"' + string + '"')
				.collect(Collectors.joining(",", "[", "]"));

		ScriptBuilder.create()
				.add("var array = " + unsortedArrayString + " as string[];")
				.add("var comparator as function(left as string, right as string) as int = (left as string, right as string) as int => left.length as int - right.length as int;")
				.add("var sortedArray = array.sorted(comparator);")
				.add("for element in array println(element);")
				.add("println('---');")
				.add("for element in sortedArray println(element);")
				.execute(this);

		logger.assertPrintOutputSize(2 * length + 1);

		int i = 0;
		for (String s : array) {
			logger.assertPrintOutput(i++, s);
		}
		logger.assertPrintOutput(i++, "---");
		array.sort(Comparator.comparing(String::length));
		for (String s : array) {
			logger.assertPrintOutput(i++, s);
		}
	}

	@Test
	public void sortWithComparatorWorks() {
		final int length = 10;

		final List<String> array = new Random().ints(length, 0, 10)
				.mapToObj(ArrayOperators::createStringWithLength)
				.collect(Collectors.toList());

		String unsortedArrayString = array.stream()
				.map(string -> '"' + string + '"')
				.collect(Collectors.joining(",", "[", "]"));

		ScriptBuilder.create()
				.add("var array = " + unsortedArrayString + " as string[];")
				.add("var comparator as function(left as string, right as string) as int = (left as string, right as string) as int => left.length as int - right.length as int;")
				.add("for element in array println(element);")
				.add("println('---');")
				.add("array.sort(comparator);")
				.add("for element in array println(element);")
				.execute(this);

		logger.assertPrintOutputSize(2 * length + 1);

		int i = 0;
		for (String s : array) {
			logger.assertPrintOutput(i++, s);
		}
		logger.assertPrintOutput(i++, "---");
		array.sort(Comparator.comparing(String::length));
		for (String s : array) {
			logger.assertPrintOutput(i++, s);
		}
	}
}
