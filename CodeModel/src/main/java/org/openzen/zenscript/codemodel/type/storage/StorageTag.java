/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.storage;

/**
 *
 * @author Hoofdgebruiker
 */
public interface StorageTag {
	StorageType getType();
	
	boolean canCastTo(StorageTag other);
	
	boolean canCastFrom(StorageTag other);
	
	boolean isDestructible();
	
	boolean isConst();
	
	boolean isImmutable();
	
	static StorageTag union(StorageTag minor, StorageTag major) {
		if (minor == AutoStorageTag.INSTANCE || minor == null)
			return major;
		if (major == AutoStorageTag.INSTANCE || major == null)
			return minor;
		if (!minor.equals(major))
			throw new IllegalArgumentException("Could not unite storage types"); // TODO: proper reporting
		
		return major;
	}
}
