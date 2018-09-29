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
public class StaticStorageTag implements StorageTag {
	public static final StaticStorageTag INSTANCE = new StaticStorageTag();
	
	private StaticStorageTag() {}

	@Override
	public StorageType getType() {
		return StaticStorageType.INSTANCE;
	}
	
	@Override
	public String toString() {
		return "static";
	}

	@Override
	public boolean canCastTo(StorageTag other) {
		return false;
	}

	@Override
	public boolean canCastFrom(StorageTag other) {
		return other == this || other == UniqueStorageTag.INSTANCE;
	}

	@Override
	public boolean isDestructible() {
		return false;
	}
}
