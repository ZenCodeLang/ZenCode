package org.openzen.zenscript.scriptingexample.tests.actual_test.functions;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

@Disabled("Requires stdlib")
public class GenericReturnTypes extends ZenCodeTest {

	@Test
	public void testListGet() {
		ScriptBuilder.create()
				.add("import stdlib.List;")
				.add("function doThing(thing as string) as void {}")
				.add("var list = new List<string>();")
				.add("list.add('thing');")
				.add("doThing(list[0]);")
				.execute(this);

		logger.assertNoErrors();
		logger.assertNoWarnings();
	}

}
