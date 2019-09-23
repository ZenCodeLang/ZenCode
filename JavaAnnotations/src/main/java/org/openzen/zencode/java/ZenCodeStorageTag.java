package org.openzen.zencode.java;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface ZenCodeStorageTag {
    
    StorageTagType value();
    
}
