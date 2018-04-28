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
public abstract class Taggable {
	private final TagDictionary tags = new TagDictionary();
	
	public final <T> void setTag(Class<T> cls, T tag) {
		tags.put(cls, tag);
	}
	
	public final <T> T getTag(Class<T> cls) {
		return tags.get(cls);
	}

	public final boolean hasTag(Class<?> cls) {
		return tags.hasTag(cls);
	}
}
