package org.openzen.zenscript.scriptingexample.tests;

import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name(".SharedGlobals")
public class SharedGlobals {

	public static PrintLogger currentlyActiveLogger;

	@ZenCodeGlobals.Global
	public static void println(String s) {
		currentlyActiveLogger.logPrintln(s);
	}

	@ZenCodeGlobals.Global
	public static @ZenCodeType.Nullable String softNullString(boolean isNull) {
		return isNull ? null : "value";
	}
}
