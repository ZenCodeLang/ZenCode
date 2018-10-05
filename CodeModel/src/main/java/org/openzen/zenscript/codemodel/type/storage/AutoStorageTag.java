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
public class AutoStorageTag implements StorageTag {
	public static final AutoStorageTag INSTANCE = new AutoStorageTag();
	
	private AutoStorageTag() {}

	@Override
	public StorageType getType() {
		return AutoStorageType.INSTANCE;
	}
	
	@Override
	public String toString() {
		return "auto";
	}

	@Override
	public boolean canCastTo(StorageTag other) {
		return other == BorrowStorageTag.INVOCATION || other == BorrowStorageTag.THIS;
	}

	@Override
	public boolean canCastFrom(StorageTag other) {
		return other == this || other == UniqueStorageTag.INSTANCE || other == StaticStorageTag.INSTANCE;
	}

	@Override
	public boolean isDestructible() {
		return true;
	}
	
	@Override
	public boolean isConst() {
		return false;
	}
	
	@Override
	public boolean isImmutable() {
		return false;
	}
}
