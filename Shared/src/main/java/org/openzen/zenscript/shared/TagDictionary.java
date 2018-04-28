/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.shared;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hoofdgebruiker
 */
@SuppressWarnings("unchecked")
public class TagDictionary {
    private Map<Class<?>, Object> tags = Collections.EMPTY_MAP;

    public <T> void put(Class<T> cls, T tag) {
        if (tags == Collections.EMPTY_MAP) {
            tags = Collections.singletonMap(cls, tag);
        } else if (tags.size() == 1) {
            Map<Class<?>, Object> newTags = new HashMap<>(tags);
            newTags.put(cls, tag);
            this.tags = newTags;
        } else {
            tags.put(cls, tag);
        }
    }

    public <T> T get(Class<T> cls) {
        return (T) tags.get(cls);
    }
}
