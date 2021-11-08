package org.openzen.zenscript.scriptingexample.tests.actual_test.parsed_expressions.floating_point_expressions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

public class FloatingPointLiteralTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(RegisteredClass.class);
		requiredClasses.add(FloatDataClass.class);
		requiredClasses.add(OtherFloatDataClass.class);
		requiredClasses.add(ExpandFloatForOtherFloatDataClass.class);
		return requiredClasses;
	}

	@ParameterizedTest
	@CsvSource({
			"F,float",
			"f,float",
			"D,double",
			"d,double",
	})
	public void FSuffixGivesFloatingPointExpression(String inbuiltSuffix, String expectedType) {
		ScriptBuilder.create()
				.add("var obj = 1.0" + inbuiltSuffix + ";")
				.add("var type = getType(obj);")
				.add("println(type);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, expectedType);
	}

	@Test
	public void NoSuffixAndNoTypeHintGivesDouble() {
		ScriptBuilder.create()
				.add("var obj = 1.0;")
				.add("var type = getType(obj);")
				.add("println(type);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "double");
	}

	@Test
	public void CustomSuffixAndTypeHintChecksForFactoryMethod() {
		ScriptBuilder.create()
				.add("var value = valueOf(10.0Data);")
				.add("println(value);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "10.0");
	}

	@Test
	public void InvalidSuffixReturnsError() {
		ScriptBuilder.create()
				.add("var x = 1.0Invalid;")
				.execute(this, ScriptBuilder.LogTolerance.ALLOW_ERRORS);

		logger.errors().assertSize(1);
		logger.errors().assertLine(0, "ERROR test_script_1.zs:1:8: test_script_1.zs:1:8: Invalid suffix: Invalid");
	}

	@Test
	public void implicitCastWorksForDoubleTypeEvenIfNoSuffixWasGiven() {
		ScriptBuilder.create()
				.add("var value = otherFloatDataToString(1.0);")
				.add("println(value);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "1.0");
	}


	@ZenCodeType.Name(".RegisteredGlobalClass")
	public static class RegisteredClass {
		@ZenCodeGlobals.Global
		@ZenCodeType.Method
		public static String getType(Object object) {
			return String.valueOf(object.getClass());
		}

		@ZenCodeGlobals.Global
		@ZenCodeType.Method
		public static String getType(float object) {
			return "float";
		}

		@ZenCodeGlobals.Global
		@ZenCodeType.Method
		public static String getType(double object) {
			return "double";
		}
	}

	@ZenCodeType.Name(".FloatDataClass")
	public static class FloatDataClass {
		private final double value;

		public FloatDataClass(double value) {

			this.value = value;
		}

		@ZenCodeType.Method
		@ZenCodeGlobals.Global //FixMe: Since the method below is global, this needs to be global as well, as Global Class != "normal" class
		public static FloatDataClass Data(double value) {
			return new FloatDataClass(value);
		}

		@ZenCodeType.Getter("value")
		public double getValue() {
			return value;
		}

		@ZenCodeType.Method
		@ZenCodeGlobals.Global
		public static String valueOf(FloatDataClass floatDataClass) {
			return String.valueOf(floatDataClass.value);
		}
	}

	@ZenCodeType.Name(".OtherFloatDataClass")
	public static class OtherFloatDataClass {

		private final double value;

		public OtherFloatDataClass(double value) {
			this.value = value;
		}

		@ZenCodeType.Method
		@ZenCodeGlobals.Global
		public static String otherFloatDataToString(OtherFloatDataClass cls) {
			return String.valueOf(cls.value);
		}
	}

	@ZenCodeType.Expansion("double")
	public static class ExpandFloatForOtherFloatDataClass {

		@ZenCodeType.Caster(implicit = true)
		public static OtherFloatDataClass toOtherFloatDataClass(double _this) {
			return new OtherFloatDataClass(_this);
		}
	}
}
