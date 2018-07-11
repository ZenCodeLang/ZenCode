package zsynthetic;

public class ArrayHelpers {
    public static <T> boolean contains(T[] haystack, T needle) {
        for (int i = 0; i < haystack.length; i++)
            if (java.util.Objects.equals(haystack[i], needle))
                return true;
        return false;
    }
}
