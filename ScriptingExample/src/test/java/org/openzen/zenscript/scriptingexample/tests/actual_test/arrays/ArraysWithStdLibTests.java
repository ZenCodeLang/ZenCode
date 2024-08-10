package org.openzen.zenscript.scriptingexample.tests.actual_test.arrays;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.*;
import java.util.stream.Collectors;

class ArraysWithStdLibTests extends ZenCodeTest {

	@Override
	public List<String> getRequiredStdLibModules() {
		return Collections.singletonList("stdlib");
	}

	// Remains a Java-Test for now, as the order of elements in the map cannot be ascertained
	@Test
	void index_string() {
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
