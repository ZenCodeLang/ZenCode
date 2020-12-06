package org.openzen.zenscript.scriptingexample.threading;

import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name("example.threading.ZCThread")
public class ZCThread {

	@ZenCodeType.Method
	public static void sleep(TimeSpan timeSpan) {
		sleep(timeSpan.getTimeMillis());
	}

	@ZenCodeType.Method
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
