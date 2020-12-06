package stdlib;

import java.util.function.Function;

public final class StringBuilderExpansion {
	public static StringBuilder shl(StringBuilder self, StringBuildable value) {
		value.toString(self);
		return self;
	}

	public static <T extends StringBuildable> StringBuilder append(StringBuilder self, Class<T> typeOfT, T[] values, String separator) {
		for (int i = 0; i < values.length; i++) {
			T value = values[i];
			if (i > 0)
				self.append(separator);
			value.toString(self);
		}
		return self;
	}

	public static <T> StringBuilder append(StringBuilder self, Class<T> typeOfT, T[] values, Function<T, String> stringer, String separator) {
		for (int i = 0; i < values.length; i++) {
			T value = values[i];
			if (i > 0)
				self.append(separator);
			self.append(stringer.apply(value));
		}
		return self;
	}
}
