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
public class ValueStorageTag implements StorageTag {
	public static final ValueStorageTag INSTANCE = new ValueStorageTag();
			
	private ValueStorageTag() {}

	@Override
	public StorageType getType() {
		return ValueStorageType.INSTANCE;
	}

	@Override
	public boolean canCastTo(StorageTag other) {
		return other == this;
	}

	@Override
	public boolean canCastFrom(StorageTag other) {
		return other == this;
	}

	@Override
	public boolean isDestructible() {
		return false;
	}
}
