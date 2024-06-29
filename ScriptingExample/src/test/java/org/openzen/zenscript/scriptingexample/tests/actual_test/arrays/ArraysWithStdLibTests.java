package org.openzen.zenscript.scriptingexample.tests.actual_test.arrays;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.*;
import java.util.stream.Collectors;

public class ArraysWithStdLibTests extends ZenCodeTest {

	private static String createStringWithLength(int length) {
		final char[] chars = new char[length];
		Arrays.fill(chars, 'A');
		return new String(chars);
	}

	@Override
	public List<String> getRequiredStdLibModules() {
		return Collections.singletonList("stdlib");
	}

	@Test
	public void reverseWorks_integers() {
		ScriptBuilder.create()
				.add("var array = [1, 2, 3, 4, 5];")
				.add("array.reverse();")
				.add("for it in array println(it);")
				.execute(this);

		logger.assertPrintOutputSize(5);

		for (int i = 5; i > 0; i--) {
			logger.assertPrintOutput(5 - i, String.valueOf(i));
		}
	}

	@Test
	public void reverseWorks_strings() {
		ScriptBuilder.create()
				.add("var array = ['1', '2', '3', '4', '5'];")
				.add("array.reverse();")
				.add("for it in array println(it);")
				.execute(this);

		logger.assertPrintOutputSize(5);

		for (int i = 5; i > 0; i--) {
			logger.assertPrintOutput(5 - i, String.valueOf(i));
		}
	}

	@Test
	public void reversedWorks_strings() {
		ScriptBuilder.create()
				.add("var array = ['1', '2', '3', '4', '5'];")
				.add("for it in array.reversed() println(it);")
				.execute(this);

		logger.assertPrintOutputSize(5);

		for (int i = 5; i > 0; i--) {
			logger.assertPrintOutput(5 - i, String.valueOf(i));
		}
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
	public void sortedWithComparatorWorks() {
		final int length = 10;

		final List<String> array = new Random().ints(length, 0, 10)
				.mapToObj(ArraysWithStdLibTests::createStringWithLength)
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
				.mapToObj(ArraysWithStdLibTests::createStringWithLength)
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

	@Test
	public void mapValues() {
		ScriptBuilder.create()
				.add("public class Stuff { public var x as string; public this(x: string){this.x = x;} }")
				.add("var array = [1, 2, 3, 4, 5] as string[];")
				.add("var mapped = array.map<Stuff>(value => new Stuff(value + value));")
				.add("for element in mapped println(element.x);")
				.execute(this);

		logger.assertPrintOutputSize(5);
		logger.assertPrintOutput(0, "11");
		logger.assertPrintOutput(1, "22");
		logger.assertPrintOutput(2, "33");
		logger.assertPrintOutput(3, "44");
		logger.assertPrintOutput(4, "55");
	}

	@Test
	public void mapKeyValues() {
		ScriptBuilder.create()
				.add("public class Stuff { public var x as string; public this(x: string){this.x = x;} }")
				.add("var array = [1, 2, 3, 4, 5] as string[];")
				.add("var mapped = array.map<Stuff>((i, value) => new Stuff(i + '~' + value));")
				.add("for element in mapped println(element.x);")
				.execute(this);

		logger.assertPrintOutputSize(5);
		logger.assertPrintOutput(0, "0~1");
		logger.assertPrintOutput(1, "1~2");
		logger.assertPrintOutput(2, "2~3");
		logger.assertPrintOutput(3, "3~4");
		logger.assertPrintOutput(4, "4~5");
	}


	@Test
	public void filterValues() {
		ScriptBuilder.create()
				.add("var array = [1, 22, 333, 4444, 55555] as string[];")
				.add("var filtered = array.filter((value) => value.length % 2 == 0);")
				.add("for element in filtered println(element);")
				.execute(this);

		logger.assertPrintOutputSize(2);
		logger.printlnOutputs().assertLinesInOrder(
				"22",
				"4444"
		);
	}

	@Test
	public void index_string() {
		ScriptBuilder.create()
				.add("var array = ['1', '2', '3', '4', '5'];")
				.add("var map = array.index<string>(element => 'index for ' + element);")
				.add("for key, value in map println(key + ' -> ' + value);")
				.execute(this);

		logger.assertPrintOutputSize(5);
		logger.printlnOutputs().assertLinesPresentInAnyOrder(
				"index for 1 -> 1",
				"index for 2 -> 2",
				"index for 3 -> 3",
				"index for 4 -> 4",
				"index for 5 -> 5"
		);
	}

}
