package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.native_template;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.SharedGlobals;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

class JavaNativeInheritanceTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(ParentClass.class);
		requiredClasses.add(ChildClass.class);
		return requiredClasses;
	}

	@Test
	void childCanBeCastToParent() {
		ScriptBuilder.create()
				.add("import test_module.ParentClass;")
				.add("import test_module.ChildClass;")
				.add("var child = new ChildClass();")
				.add("var parent = child as ParentClass;")
				.add("println(parent);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"ChildClass"
		);
	}

	@Test
	void fieldsAreInheritedFromParent() {
		ScriptBuilder.create()
				.add("import test_module.ChildClass;")
				.add("var child = new ChildClass();")
				.add("println(child.someValue);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Some Value"
		);
	}

	@Test
	void methodsAreInheritedFromParent() {
		ScriptBuilder.create()
				.add("import test_module.ChildClass;")
				.add("var child = new ChildClass();")
				.add("println(child.greetMe());")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Hi from child"
		);
	}

	@Test
	void gettersAreInheritedFromParent() {
		ScriptBuilder.create()
				.add("import test_module.ChildClass;")
				.add("var child = new ChildClass();")
				.add("println(child.dayOfWeek);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Monday"
		);
	}

	@Test
	void settersAreInheritedFromParent() {
		ScriptBuilder.create()
				.add("import test_module.ChildClass;")
				.add("var child = new ChildClass();")
				.add("child.dayOfWeek = 'Tuesday';")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Before: Monday, After: Tuesday"
		);
	}

	@Test
	void operatorsAreInheritedFromParent() {
		ScriptBuilder.create()
				.add("import test_module.ChildClass;")
				.add("var child = new ChildClass();")
				.add("println(child * 2.0d);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"4.0"
		);
	}

	@Test
	void casterIsAvailableOnParent() {
		ScriptBuilder.create()
				.add("import test_module.ParentClass;")
				.add("var instance = new ParentClass();")
				.add("println(instance);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"ParentClass"
		);
	}

	@Test
	void casterIsInheritedFromParent() {
		ScriptBuilder.create()
				.add("import test_module.ChildClass;")
				.add("var child = new ChildClass();")
				.add("println(child);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"ChildClass"
		);
	}

	@ZenCodeType.Name("test_module.ParentClass")
	public static class ParentClass {

		@ZenCodeType.Field
		public final String someValue = "Some Value";

		@ZenCodeType.Constructor
		public ParentClass() {
			// default .ctor
		}

		@ZenCodeType.Method
		public String greetMe() {
			return "Hi from parent";
		}

		@ZenCodeType.Method
		public String sayHello() {
			return "Hello from parent";
		}

		@ZenCodeType.Getter("dayOfWeek")
		public String getDayOfWeek() {
			return "Monday";
		}

		@ZenCodeType.Setter("dayOfWeek")
		public void setDayOfWeek(String dayOfWeek) {
			SharedGlobals.println("Before: Monday, After: " + dayOfWeek);
		}

		@ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
		public double doubleTheValue(double value) {
			return value * 2;
		}

		@ZenCodeType.Caster(implicit = true)
		public String asString() {
			return "ParentClass";
		}
	}

	@ZenCodeType.Name("test_module.ChildClass")
	public static class ChildClass extends ParentClass {

		@ZenCodeType.Constructor
		public ChildClass() {
			// default .ctor
		}

		@Override
		public String greetMe() {
			return "Hi from child";
		}

		@Override
		public String asString() {
			return "ChildClass";
		}
	}
}
