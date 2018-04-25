/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.shared;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Hoofdgebruiker
 */
public class TagDictionary {
	private final Map<Class<?>, Object> tags = new HashMap<>();
	
	public <T> void put(Class<T> cls, T tag) {
		tags.put(cls, tag);
	}
	
	public <T> T get(Class<T> cls) {
		return (T) tags.get(cls);
	}
}
