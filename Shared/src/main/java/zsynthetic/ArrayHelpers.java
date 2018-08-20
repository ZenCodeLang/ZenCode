package zsynthetic;

public class ArrayHelpers {
    public static <T> boolean contains(T[] haystack, T needle) {
        for (int i = 0; i < haystack.length; i++)
            if (haystack[i].equals(needle))
                return true;
        return false;
    }
}
