package stdlib;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import zsynthetic.FunctionUSizeTToBool;
import zsynthetic.FunctionUSizeTToU;
import zsynthetic.FunctionUSizeTToVoid;

public final class Arrays {
    private Arrays() {}
    public static <T> T getFirst(Class<T> typeOfT, T[] self) {
        return self.length == 0 ? null : self[0];
    }
    
    public static <T> T getLast(Class<T> typeOfT, T[] self) {
        return self.length == 0 ? null : self[self.length - 1];
    }
    
    public static <T> void reverse(Class<T> typeOfT, T[] self) {
        int limitForI = self.length / 2;
        for (int i = 0; i < limitForI; i++) {
            T temp = self[i];
            self[i] = self[self.length - i - 1];
            self[self.length - i - 1] = temp;
        }
    }
    
    public static <U, T> U[] map(Class<T> typeOfT, T[] self, Class<U> typeOfU, Function<T, U> projection) {
        U[] temp1 = (U[])(Array.newInstance(typeOfU, self.length));
        for (int temp2 = 0; temp2 < temp1.length; temp2++)
            temp1[temp2] = projection.apply(self[temp2]);
        return temp1;
    }
    
    public static <U, T> U[] map(Class<T> typeOfT, T[] self, Class<U> typeOfU, FunctionUSizeTToU<T, U> projection) {
        U[] temp1 = (U[])(Array.newInstance(typeOfU, self.length));
        for (int temp2 = 0; temp2 < temp1.length; temp2++)
            temp1[temp2] = projection.invoke(temp2, self[temp2]);
        return temp1;
    }
    
    public static <T> T[] filter(Class<T> typeOfT, T[] self, Predicate<T> predicate) {
        List<T> values = new ArrayList<T>();
        for (T value : self)
            if (predicate.test(value))
                values.add(value);
        return values.toArray((T[])(Array.newInstance(typeOfT, values.size())));
    }
    
    public static <T> T[] filter(Class<T> typeOfT, T[] self, FunctionUSizeTToBool<T> predicate) {
        List<T> values = new ArrayList<T>();
        for (int i = 0; i < self.length; i++) {
            T value = self[i];
            if (predicate.invoke(i, value))
                values.add(value);
        }
        return values.toArray((T[])(Array.newInstance(typeOfT, values.size())));
    }
    
    public static <T> void each(Class<T> typeOfT, T[] self, Consumer<T> consumer) {
        for (T value : self)
            consumer.accept(value);
    }
    
    public static <T> void each(Class<T> typeOfT, T[] self, FunctionUSizeTToVoid<T> consumer) {
        for (int i = 0; i < self.length; i++) {
            T value = self[i];
            consumer.invoke(i, value);
        }
    }
    
    public static <T> boolean contains(Class<T> typeOfT, T[] self, Predicate<T> predicate) {
        for (T value : self)
            if (predicate.test(value))
                return true;
        return false;
    }
    
    public static <T> boolean contains(Class<T> typeOfT, T[] self, FunctionUSizeTToBool<T> predicate) {
        for (int i = 0; i < self.length; i++) {
            T value = self[i];
            if (predicate.invoke(i, value))
                return true;
        }
        return false;
    }
    
    public static <T> boolean all(Class<T> typeOfT, T[] self, Predicate<T> predicate) {
        for (T value : self)
            if (!predicate.test(value))
                return false;
        return true;
    }
    
    public static <T> boolean all(Class<T> typeOfT, T[] self, FunctionUSizeTToBool<T> predicate) {
        for (int i = 0; i < self.length; i++) {
            T value = self[i];
            if (!predicate.invoke(i, value))
                return false;
        }
        return true;
    }
    
    public static <T> T first(Class<T> typeOfT, T[] self, Predicate<T> predicate) {
        for (T value : self)
            if (predicate.test(value))
                return value;
        return null;
    }
    
    public static <T> T first(Class<T> typeOfT, T[] self, FunctionUSizeTToBool<T> predicate) {
        for (int i = 0; i < self.length; i++) {
            T value = self[i];
            if (predicate.invoke(i, value))
                return value;
        }
        return null;
    }
    
    public static <T> T last(Class<T> typeOfT, T[] self, Predicate<T> predicate) {
        int i = self.length;
        while (i > 0) {
            i--;
            if (predicate.test(self[i]))
                return self[i];
        }
        return null;
    }
    
    public static <T> T last(Class<T> typeOfT, T[] self, FunctionUSizeTToBool<T> predicate) {
        int i = self.length;
        while (i > 0) {
            i--;
            if (predicate.invoke(i, self[i]))
                return self[i];
        }
        return null;
    }
    
    public static <T> int count(Class<T> typeOfT, T[] self, Predicate<T> predicate) {
        int result = 0;
        for (T value : self)
            if (predicate.test(value))
                result++;
        return result;
    }
    
    public static <T> int count(Class<T> typeOfT, T[] self, FunctionUSizeTToBool<T> predicate) {
        int result = 0;
        for (int i = 0; i < self.length; i++) {
            T value = self[i];
            if (predicate.invoke(i, value))
                result++;
        }
        return result;
    }
    
    public static <K, T> Map<K, T> index(Class<T> typeOfT, T[] self, Class<K> typeOfK, Function<T, K> key) {
        Map<K, T> result = new HashMap<>();
        for (T value : self)
            result.put(key.apply(value), value);
        return result;
    }
}