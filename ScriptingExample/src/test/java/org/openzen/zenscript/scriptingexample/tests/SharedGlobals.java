package org.openzen.zenscript.scriptingexample.tests;

import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTestLogger;

@ZenCodeType.Name(".SharedGlobals")
public class SharedGlobals {

	public static PrintLogger currentlyActiveLogger;

	@ZenCodeGlobals.Global
	public static void println(String s) {
		currentlyActiveLogger.logPrintln(s);
	}

	@ZenCodeGlobals.Global
	public static String softNullString(boolean null_) {
		return null_ ? null : "value";
	}
}
