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
	public void basic() {
		ScriptBuilder.create()
				.add("var result = [1, 2, 3];")
				.add("println(result[0]);")
				.add("println(result.length);")
				.execute(this);

		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "1");
		logger.assertPrintOutput(1, "3");
	}

	@Test
	public void sized() {
		ScriptBuilder.create()
				.add("var result = new int[](1);")
				.add("println(result[0]);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "0");
	}

	@Test
	public void sizedWithDefaultValue() {
		ScriptBuilder.create()
				.add("var x = new int[](10, 8);")
				.add("println(x[5]);")
				.add("println(x.length);")
				.execute(this);

		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "8");
		logger.assertPrintOutput(1, "10");
	}

	@Test
	public void multiDimSized() {
		ScriptBuilder.create()
				.add("var x = new int[,](10, 10);")
				.add("println(x[5, 0]);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "0");
	}

	@Test
	public void multiDimSizedWithDefault() {
		ScriptBuilder.create()
				.add("var x = new int[,](10, 10, 7);")
				.add("println(x[5, 0]);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "7");
	}

	@Test
	public void callback() {
		ScriptBuilder.create()
				.add("var result = new int[](10, (index as usize) => 4);")
				.add("println(result[0]);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "4");
	}

	@Test
	public void projection() {
		ScriptBuilder.create()
				.add("var x = [9, 8, 7] as int[];")
				.add("var y = new int[]<int>(x, (xVal => 10 * xVal) as function(xVal as int) as int);")
				.add("println(y[0]);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "90");
	}

	@Test
	public void projectionWithIndex() {
		ScriptBuilder.create()
				.add("var x = [9, 8, 7] as int[];")
				.add("var y = new int[]<int>(x, ((index, xVal) => 10 * xVal) as function(index as usize, xVal as int) as int);")
				.add("println(y[0]);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "90");
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
