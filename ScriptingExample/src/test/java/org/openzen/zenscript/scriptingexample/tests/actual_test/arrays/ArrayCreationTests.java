package org.openzen.zenscript.scriptingexample.tests.actual_test.arrays;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ArrayCreationTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(SuperClass.class);
		requiredClasses.add(ChildClass.class);
		requiredClasses.add(UsingClass.class);
		return requiredClasses;
	}

	@Test
	void varargCreationShouldUseProperType() {
		ScriptBuilder.create()
				.add("var result = useSuperClass(createChildClass(), createChildClass());")
				.add("println(result);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "[ChildClass, ChildClass]");
	}

	@Test
	void varargCreationShouldUseProperTypeParents() {
		ScriptBuilder.create()
				.add("var result = useSuperClass(createSuperClass(), createSuperClass());")
				.add("println(result);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "[SuperClass, SuperClass]");
	}

	@Test
	void varargExplicitArrayShouldUseProperType() {
		ScriptBuilder.create()
				.add("var result = useSuperClass([createChildClass(), createChildClass()]);")
				.add("println(result);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "[ChildClass, ChildClass]");
	}

	@Test
	void varargMixedArrayShouldUseProperType() {
		ScriptBuilder.create()
				.add("var result = useSuperClass(createChildClass(), createSuperClass());")
				.add("println(result);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "[ChildClass, SuperClass]");
	}

	@Test
	void varargExplicitMixedArrayShouldUseProperType() {
		ScriptBuilder.create()
				.add("var result = useSuperClass([createChildClass(), createSuperClass()]);")
				.add("println(result);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "[ChildClass, SuperClass]");
	}


	@ZenCodeType.Name(".SuperClass")
	public static class SuperClass {

		@ZenCodeType.Getter("type")
		public String getType() {
			return "SuperClass";
		}
	}
	@ZenCodeType.Name(".ChildClass")
	public static class ChildClass extends SuperClass{

		@Override
		public String getType() {
			return "ChildClass";
		}
	}

	@ZenCodeType.Name(".UsingClass")
	public static final class UsingClass {
		@ZenCodeGlobals.Global
		public static String useSuperClass(SuperClass... args) {
			return Arrays.stream(args).map(SuperClass::getType).collect(Collectors.joining(", ", "[", "]"));
		}

		@ZenCodeGlobals.Global
		public static SuperClass createSuperClass() {
			return new SuperClass();
		}

		@ZenCodeGlobals.Global
		public static ChildClass createChildClass() {
			return new ChildClass();
		}
	}
}
