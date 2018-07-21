package stdlib;

import java.util.HashMap;
import java.util.Map;
import zsynthetic.Function8;

public final class Assoc {
    private Assoc() {}
	static <W, K, V> Map<K, W> mapValues(Class<K> typeOfK, Class<V> typeOfV, Map<K, V> self, Class<W> typeOfW, Function8<W, V> projection) {
	    Map<K, W> result = new HashMap<>();
	    for (Map.Entry<K, V> temp1 : self.entrySet()) {
	        K k = temp1.getKey();
	        V v = temp1.getValue();
	        
	        result.put(k, projection.invoke(v));
	    }
	    return result;
	}
}