package org.openzen.zenscript.scriptingexample;

import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenCodeType.Name(".MyFunctionalInterfaceClass")
public interface MyFunctionalInterfaceClass {
	@ZenCodeType.Method
	String doSomething(String arg);
}
