package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.enums;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

public class EnumMemberTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(MyEnum.class);
		requiredClasses.add(GlobalEnumUser.class);
		return requiredClasses;
	}

	@Test
	public void EnumMembersMustBeRetrievableByTypeHintAndName() {
		ScriptBuilder.create()
				.add("println(getName(A));")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "A");
	}


	@ZenCodeType.Name("test_module.MyEnum")
	public enum MyEnum {
		A, B, C
	}

	@ZenCodeType.Name("test_module.GlobalEnumUser")
	public static final class GlobalEnumUser {
		@ZenCodeGlobals.Global
		public static String getName(MyEnum myEnum) {
			return myEnum.name();
		}
	}
}
