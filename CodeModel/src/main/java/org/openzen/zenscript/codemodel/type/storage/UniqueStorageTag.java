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
public class UniqueStorageTag implements StorageTag {
	public static final UniqueStorageTag INSTANCE = new UniqueStorageTag() {};
			
	private UniqueStorageTag() {}

	@Override
	public StorageType getType() {
		return UniqueStorageType.INSTANCE;
	}
	
	@Override
	public String toString() {
		return "unique";
	}

	@Override
	public boolean canCastTo(StorageTag other) {
		return false;
	}

	@Override
	public boolean canCastFrom(StorageTag other) {
		return other == this;
	}

	@Override
	public boolean isDestructible() {
		return true;
	}
}
