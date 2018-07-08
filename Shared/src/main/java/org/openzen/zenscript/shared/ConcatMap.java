/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.shared;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConcatMap<K, V> {
	private static final ConcatMap EMPTY = new ConcatMap(null, null, null);
	
	public static final <K, V> ConcatMap<K, V> empty() {
		return EMPTY;
	}
	
	private final K key;
	private final V value;
	private final ConcatMap<K, V> remaining;
	
	private ConcatMap(K key, V value, ConcatMap<K, V> remaining) {
		this.key = key;
		this.value = value;
		this.remaining = remaining;
	}
	
	public boolean isEmpty() {
		return key == null;
	}
	
	public ConcatMap<K, V> concat(K key, V value) {
		return new ConcatMap(key, value, this);
	}
	
	public boolean containsKey(K key) {
		if (this.key == null)
			return false;
		if (key.equals(this.key))
			return true;
		
		if (remaining == null)
			return false;
		
		return remaining.containsKey(key);
	}
	
	public V get(K key) {
		if (this.key == null)
			return null;
		if (key.equals(this.key))
			return value;
		
		return remaining.get(key);
	}
	
	public V get(K key, V defaultValue) {
		if (this.key == null)
			return defaultValue;
		if (key.equals(this.key))
			return value;
		
		return remaining.get(key);
	}
}
