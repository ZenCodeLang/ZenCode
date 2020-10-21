/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import java.lang.reflect.Method;
import java.util.*;

import org.openzen.zenscript.codemodel.type.storage.AutoStorageTag;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.codemodel.type.storage.SharedStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageType;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageTag;

public class JavaFunctionalInterfaceStorageTag implements StorageTag {
	public final Method functionalInterfaceMethod;
	public final JavaMethod method;
	
	public JavaFunctionalInterfaceStorageTag(Method functionalInterfaceMethod, JavaMethod method) {
		this.functionalInterfaceMethod = functionalInterfaceMethod;
		this.method = method;
	}

	@Override
	public StorageType getType() {
		return JavaFunctionalInterfaceStorageType.INSTANCE;
	}

	@Override
	public boolean canCastTo(StorageTag other) {
		return other instanceof JavaFunctionalInterfaceStorageTag
				|| other instanceof AutoStorageTag
				|| other instanceof SharedStorageTag
				|| other instanceof BorrowStorageTag;
	}

	@Override
	public boolean canCastFrom(StorageTag other) {
		return other instanceof JavaFunctionalInterfaceStorageTag
				|| other instanceof AutoStorageTag
				|| other instanceof SharedStorageTag
				|| other instanceof UniqueStorageTag;
	}

	@Override
	public boolean isDestructible() {
		return false;
	}

	@Override
	public boolean isConst() {
		return true;
	}

	@Override
	public boolean isImmutable() {
		return true;
	}
    
    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        
        JavaFunctionalInterfaceStorageTag other = (JavaFunctionalInterfaceStorageTag) o;
        return Objects.equals(functionalInterfaceMethod, other.functionalInterfaceMethod);
    }
    
    @Override
    public int hashCode() {
        return functionalInterfaceMethod != null ? functionalInterfaceMethod.hashCode() : 0;
    }
}
