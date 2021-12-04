package org.openzen.zenscript.scriptingexample.tests.actual_test.arrays;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayCreationTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(SuperClass.class);
		requiredClasses.add(ChildClass.class);
		requiredClasses.add(UsingClass.class);
		return requiredClasses;
	}

	@Test
	public void varargCreationShouldUseProperType() {
		ScriptBuilder.create()
				.add("var result = useSuperClass(createChildClass(), createChildClass());")
				.add("println(result);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "[ChildClass, ChildClass]");
	}

	@Test
	public void varargExplicitArrayShouldUseProperType() {
		ScriptBuilder.create()
				.add("var result = useSuperClass([createChildClass(), createChildClass()]);")
				.add("println(result);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "[ChildClass, ChildClass]");
	}

	@Test
	public void varargMixedArrayShouldUseProperType() {
		ScriptBuilder.create()
				.add("var result = useSuperClass(createChildClass(), createSuperClass());")
				.add("println(result);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "[ChildClass, SuperClass]");
	}

	@Test
	public void varargExplicitMixedArrayShouldUseProperType() {
		ScriptBuilder.create()
				.add("var result = useSuperClass([createChildClass(), createSuperClass()]);")
				.add("println(result);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "[ChildClass, SuperClass]");
	}


	@ZenCodeType.Name("test_module.SuperClass")
	public static class SuperClass {

		@ZenCodeType.Getter("type")
		public String getType() {
			return "SuperClass";
		}
	}
	@ZenCodeType.Name("test_module.ChildClass")
	public static class ChildClass extends SuperClass{

		@Override
		public String getType() {
			return "ChildClass";
		}
	}

	@ZenCodeType.Name("test_module.UsingClass")
	public static final class UsingClass {
		@ZenCodeGlobals.Global
		public static String useSuperClass( SuperClass... args) {
			return Arrays.stream(args).map(SuperClass::getType).collect(Collectors.joining(", ", "[", "]"));
		}

		@ZenCodeGlobals.Global
		@ZenCodeType.Method
		public static SuperClass createSuperClass() {
			return new SuperClass();
		}

		@ZenCodeGlobals.Global
		@ZenCodeType.Method
		public static ChildClass createChildClass() {
			return new ChildClass();
		}
	}


}
