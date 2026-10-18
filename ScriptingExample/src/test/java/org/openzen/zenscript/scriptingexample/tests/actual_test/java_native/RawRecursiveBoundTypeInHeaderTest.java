package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

/**
 * Raw types whose type parameter bound leads back to the type itself (T extends SelfBoundedGenClass<T>).
 * Without the guard in JavaRuntimeTypeConverterImpl.loadRawClass, the bound would either recurse or leak the
 * guard's placeholder type parameter, so the round trip below would no longer type-check.
 * No other test registers these types, so a failure here points at that guard.
 */
public class RawRecursiveBoundTypeInHeaderTest extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(SelfBoundedGenClass.class);
		requiredClasses.add(TestGlobals.class);
		return requiredClasses;
	}

	@Test
	void rawSelfBoundedTypeRoundTrips() {
		addScript("println(acceptRawSelfBounded(makeRawSelfBounded()));");

		executeEngine();

		logger.assertNoErrors();
		logger.printlnOutputs().assertLinesInOrder("self bounded");
	}

	@ZenCodeType.Name("test_module.java_native.RawRecursiveBoundTestGlobals")
	public static final class TestGlobals {
		@ZenCodeGlobals.Global
		public static SelfBoundedGenClass makeRawSelfBounded() {
			return new SelfBoundedGenClass<>();
		}

		@ZenCodeGlobals.Global
		public static String acceptRawSelfBounded(SelfBoundedGenClass clazz) {
			return "self bounded";
		}
	}

	@ZenCodeType.Name("test_module.java_native.SelfBoundedGenClass")
	public static final class SelfBoundedGenClass<T extends SelfBoundedGenClass<T>> {
	}
}
