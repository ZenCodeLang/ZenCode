package zsynthetic;

public class ArrayHelpers {
	public static boolean containsChar(char[] haystack, char needle) {
		for (int i = 0; i < haystack.length; i++)
			if (haystack[i] == needle)
				return true;
		return false;
	}
}
