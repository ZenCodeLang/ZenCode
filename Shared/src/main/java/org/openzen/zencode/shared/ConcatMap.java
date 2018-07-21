package org.openzen.zencode.shared;

import stdlib.EqualsComparable;

public final class ConcatMap<K extends EqualsComparable<K>, V>  {
	public static final <K extends EqualsComparable<K>, V> ConcatMap<K, V> empty(Class<K> typeOfK, Class<V> typeOfV) {
	    return new ConcatMap<K, V>(null, null, null);
	}
	
	private final K key;
	private final V value;
	private final ConcatMap<K, V> remaining;
	
	private ConcatMap(K key, V value, ConcatMap<K, V> remaining) {
	    this.key = key;
	    this.value = value;
	    this.remaining = remaining;
	}
	
	public boolean getIsEmpty() {
	    return key == null;
	}
	
	public ConcatMap<K, V> concat(K key, V value) {
	    return new ConcatMap<K, V>(key, value, this);
	}
	
	public boolean contains(K key) {
	    if (this.key == null)
	        return false;
	    if (key.equals_(this.key))
	        return true;
	    if (remaining == null)
	        return false;
	    return remaining.contains(key);
	}
	
	public V getAt(K key) {
	    if (this.key == null)
	        return null;
	    if (key.equals_(this.key))
	        return value;
	    return remaining.getAt(key);
	}
	
	public V getOrDefault(K key, V defaultValue) {
	    if (this.key == null)
	        return defaultValue;
	    if (key.equals_(this.key))
	        return value;
	    return remaining.getOrDefault(key, defaultValue);
	}
}
