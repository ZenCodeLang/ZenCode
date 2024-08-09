package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.native_template;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;


class JavaNativeSpecialTypeTests extends ZenCodeTest {

	@Override
	public List<String> getRequiredStdLibModules() {
		return Collections.singletonList("stdlib");
	}

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(ClassWithCollectionMember.class);
		requiredClasses.add(ClassWithObjectMember.class);
		return requiredClasses;
	}

	@Test
	void canUseCollectionsAndAccessSize() {
		ScriptBuilder.create()
				.add("import test_module.java_native.native_template.ClassWithCollectionMember;")
				.add("var values = ClassWithCollectionMember.getValues();")
				.add("println(values.length);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"3"
		);
	}

	@Test
	void canUseCollectionsAndIterateOverValues() {
		ScriptBuilder.create()
				.add("import test_module.java_native.native_template.ClassWithCollectionMember;")
				.add("var values = ClassWithCollectionMember.getValues();")
				.add("for value in values {")
				.add("  println(value);")
				.add("}")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"a",
				"b",
				"c"
		);
	}

	@Test
	void canUseObject() {
		ScriptBuilder.create()
				.add("import test_module.java_native.native_template.ClassWithObjectMember;")
				.add("var instance = new ClassWithObjectMember();")
				.add("var other = new ClassWithObjectMember();")
				.add("println(instance.equals(other));")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"true"
		);
	}

	@ZenCodeType.Name("test_module.java_native.native_template.ClassWithCollectionMember")
	public static final class ClassWithCollectionMember {

		@ZenCodeType.Method
		public static Collection<String> getValues() {
			LinkedHashSet<String> result = new LinkedHashSet<>();
			result.add("a");
			result.add("b");
			result.add("c");
			return result;
		}
	}

	@ZenCodeType.Name("test_module.java_native.native_template.ClassWithObjectMember")
	public static final class ClassWithObjectMember {

		@ZenCodeType.Constructor
		public ClassWithObjectMember() {
			// default .ctor
		}

		// ToDo: remove this once other types are auto-castable to Object
		@ZenCodeType.Caster(implicit = true)
		public Object asObject() {
			return this;
		}

		@Override
		@ZenCodeType.Method
		public boolean equals(Object other) {
			return other instanceof ClassWithObjectMember;
		}
	}
}
