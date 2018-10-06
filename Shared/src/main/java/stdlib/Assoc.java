package stdlib;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class Assoc {
    private Assoc() {}
	static <W, K, V> Map<K, W> mapValues(Class<K> typeOfK, Class<V> typeOfV, Map<K, V> self, Class<W> typeOfW, Function<V, W> projection) {
	    Map<K, W> result = new HashMap<>();
	    for (Map.Entry<K, V> temp1 : self.entrySet()) {
	        K k = temp1.getKey();
	        V v = temp1.getValue();
	        result.put(k, projection.apply(v));
	    }
	    return result;
	}
}