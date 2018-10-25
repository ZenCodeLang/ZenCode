/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.storage.AutoStorageTag;
import org.openzen.zenscript.codemodel.type.storage.SharedStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaTypeUtils {
	private JavaTypeUtils() {}
	
	public static boolean isShared(StoredType type) {
		return type.type.isDestructible() && isShared(type.getActualStorage());
	}
	
	public static boolean isShared(StorageTag storage) {
		return storage == SharedStorageTag.INSTANCE || storage == AutoStorageTag.INSTANCE;
	}
}
