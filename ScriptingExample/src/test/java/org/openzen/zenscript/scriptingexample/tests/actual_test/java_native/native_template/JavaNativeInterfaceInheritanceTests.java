package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.native_template;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

class JavaNativeInterfaceInheritanceTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(GrandparentInterface.class);
		requiredClasses.add(ParentInterface.class);
		requiredClasses.add(ChildClass.class);
		requiredClasses.add(ExpansionToParent.class);
		return requiredClasses;
	}

	@Test
	void childCanBeCastToParent() {
		ScriptBuilder.create()
				.add("import test_module.java_native.native_template.ParentInterface;")
				.add("import test_module.java_native.native_template.ChildClass;")
				.add("var child = new ChildClass();")
				.add("var parent = child as ParentInterface;")
				.add("println(parent.sayHello());")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Hello from child"
		);
	}

	@Test
	void methodFromParentIsAvailableOnChild() {
		ScriptBuilder.create()
				.add("import test_module.java_native.native_template.ChildClass;")
				.add("var child = new ChildClass();")
				.add("println(child.sayHello());")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Hello from child"
		);
	}

	@Test
	void defaultMethodFromParentIsAvailableOnChild_overridden() {
		ScriptBuilder.create()
				.add("import test_module.java_native.native_template.ChildClass;")
				.add("var child = new ChildClass();")
				.add("println(child.sayGoodbye());")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Goodbye from child"
		);
	}

	@Test
	void defaultMethodFromParentIsAvailableOnChild_notOverridden() {
		ScriptBuilder.create()
				.add("import test_module.java_native.native_template.ChildClass;")
				.add("var child = new ChildClass();")
				.add("println(child.saySomethingElse());")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Something else from parent"
		);
	}

	@Test
	void methodOnlyInChildRemainsCallable() {
		ScriptBuilder.create()
				.add("import test_module.java_native.native_template.ChildClass;")
				.add("var child = new ChildClass();")
				.add("println(child.methodOnlyInChild());")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Method only in child"
		);
	}

	@Test
	void childCanBeCastToGrandParent() {
		ScriptBuilder.create()
				.add("import test_module.java_native.native_template.GrandparentInterface;")
				.add("import test_module.java_native.native_template.ChildClass;")
				.add("var child = new ChildClass();")
				.add("var grandparent = child as GrandparentInterface;")
				.add("println(grandparent.commandString);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"<childClass>"
		);
	}

	@Test
	void methodsFromGrandparentInterfaceAvailableOnChild() {
		ScriptBuilder.create()
				.add("import test_module.java_native.native_template.ChildClass;")
				.add("var child = new ChildClass();")
				.add("println(child.commandString);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"<childClass>"
		);
	}

	@Test
	void expansionFromParentInterfaceAvailableOnChild() {
		ScriptBuilder.create()
				.add("import test_module.java_native.native_template.ChildClass;")
				.add("val child = new ChildClass();")
				.add("val info = child.getInfo();")
				.add("println(info);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Info for Expansion"
		);
	}

	@ZenCodeType.Name("test_module.java_native.native_template.GrandparentInterface")
	public interface GrandparentInterface {
		@ZenCodeType.Getter("commandString")
		String getCommandString();
	}

	@ZenCodeType.Name("test_module.java_native.native_template.ParentInterface")
	public interface ParentInterface extends GrandparentInterface {
		@ZenCodeType.Method
		String sayHello();

		@ZenCodeType.Method
		default String sayGoodbye() {
			return "Goodbye from parent";
		}

		@ZenCodeType.Method
		default String saySomethingElse() {
			return "Something else from parent";
		}
	}

	@ZenCodeType.Name("test_module.java_native.native_template.ChildClass")
	public static class ChildClass implements ParentInterface {

		@ZenCodeType.Constructor
		public ChildClass() {
			// default .ctor
		}

		@Override
		public String sayHello() {
			return "Hello from child";
		}

		@Override
		public String sayGoodbye() {
			return "Goodbye from child";
		}

		@ZenCodeType.Method
		public String methodOnlyInChild() {
			return "Method only in child";
		}

		@Override
		public String getCommandString() {
			return "<childClass>";
		}
	}

	@ZenCodeType.Expansion(".java_native.native_template.ParentInterface")
	public static class ExpansionToParent {

		@ZenCodeType.Method
		public static String getInfo(ParentInterface value) {
			return "Info for Expansion";
		}
	}
}
