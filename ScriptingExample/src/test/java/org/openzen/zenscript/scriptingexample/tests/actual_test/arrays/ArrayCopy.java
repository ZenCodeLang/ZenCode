package org.openzen.zenscript.scriptingexample.tests.actual_test.arrays;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class ArrayCopy extends ZenCodeTest {

	@ParameterizedTest
	@ValueSource(strings = {"int", "int?", "string", "string?"})
	public void canCloneArray(String baseType) {
		ScriptBuilder.create()
				.add("var oldArray = [0, 1, 2, 3, 4] as " + baseType + "[];")
				.add("var newArray = oldArray.copy();")
				.add("newArray[0] = 99;")
				.add("println(newArray[0]);")
				.add("println(oldArray[0]);")
				.execute(this);

		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "99");
		logger.assertPrintOutput(1, "0");
	}

	@ParameterizedTest
	@ValueSource(strings = {"int", "int?", "string", "string?"})
	public void canCopyResize(String baseType) {
		final int newSize = 3;

		ScriptBuilder.create()
				.add("var oldArray = [0, 1, 2, 3, 4] as " + baseType + "[];")
				.add("var newArray = oldArray.copy(" + newSize + ");")
				.add("newArray[0] = 99;")
				.add("println(newArray[0]);")
				.add("println(oldArray[0]);")
				.add("println(newArray.length);")
				.add("println(oldArray.length);")
				.execute(this);

		logger.assertPrintOutputSize(4);
		logger.assertPrintOutput(0, "99");
		logger.assertPrintOutput(1, "0");
		logger.assertPrintOutput(2, "3");
		logger.assertPrintOutput(3, "5");
	}
}
