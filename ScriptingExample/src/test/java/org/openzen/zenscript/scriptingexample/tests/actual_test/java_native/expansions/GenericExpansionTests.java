package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.expansions;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.Collections;
import java.util.List;

class GenericExpansionTests extends ZenCodeTest {

	@Override
	public List<String> getRequiredStdLibModules() {
		return Collections.singletonList("stdlib");
	}

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(ExpandStringList.class);
		return requiredClasses;
	}

	@Test
	void works() {
		ScriptBuilder.create()
				.add("import stdlib.List;")
				.add("var list = ['one', 'two', 'three'] as List<string>;")
				.add("println(list.join(','));")
				.execute(this);
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "one,two,three");
	}

	@ZenCodeType.Expansion(value = "stdlib.List<T>", typeParameters = "<T : string>")
	public static class ExpandStringList {


		@ZenCodeType.Method
		public static String join(List<String> list) {
			return String.join(",", list);
		}
	}
}
