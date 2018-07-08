package stdlib;


public final class StringBuilder {
	public static StringBuilder shl(StringBuilder self, StringBuildable value) {
	    value.toString(self);
	    return self;
	}
}
