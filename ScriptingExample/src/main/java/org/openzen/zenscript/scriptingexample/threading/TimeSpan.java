package org.openzen.zenscript.scriptingexample.threading;

import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name("example.threading.TimeSpan")
public class TimeSpan {
	private final long timeNanos;

	public TimeSpan(long timeNanos) {
		this.timeNanos = timeNanos;
	}

	@ZenCodeType.Getter("timeMillis")
	public long getTimeMillis() {
		return timeNanos;
	}

	@ZenCodeType.Expansion("int")
	public static final class ExpandInt {
		@ZenCodeType.Method
		public static TimeSpan seconds(int _this) {
			return new TimeSpan(_this * 1_000);
		}

		@ZenCodeType.Method
		@ZenCodeType.Caster(implicit = true)
		public static TimeSpan milliSeconds(int _this) {
			return new TimeSpan(_this);
		}

	}
}
