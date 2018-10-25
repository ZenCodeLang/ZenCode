package org.openzen.zencode.shared;

import java.util.HashMap;
import java.util.Map;

public class Taggable {
    private final Map<Class<?>, Object> tags = new HashMap<Class<?>, Object>();
    
    public <T extends Tag> void setTag(Class<T> typeOfT, T tag) {
        tags.put(typeOfT, tag);
    }
    
    public <T extends Tag> T getTag(Class<T> typeOfT) {
        return (T)(tags.get(typeOfT));
    }
    
    public <T extends Tag> boolean hasTag(Class<T> typeOfT) {
        return tags.containsKey(typeOfT);
    }
    
    public void addAllTagsFrom(Taggable other) {
        tags.putAll(other.tags);
    }
}
