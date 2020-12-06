package org.openzen.zenscript.scriptingexample.tests;

import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTestLogger;

public class SharedGlobals {

	public static ZenCodeTestLogger currentlyActiveLogger;

	@ZenCodeGlobals.Global
	public static void println(String s) {
		currentlyActiveLogger.logPrintln(s);
	}
}
