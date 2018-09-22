/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.storage;

import org.openzen.zencode.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public interface StorageType {
	public static StorageType[] getStandard() {
		return new StorageType[] {
			AnyStorageType.INSTANCE,
			BorrowStorageType.INSTANCE,
			SharedStorageType.INSTANCE,
			StaticStorageType.INSTANCE,
			UniqueStorageType.INSTANCE
		};
	}
	
	String getName();
	
	StorageTag instance(CodePosition position, String[] arguments);
}
