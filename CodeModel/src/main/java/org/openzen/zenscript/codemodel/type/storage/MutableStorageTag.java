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
public class MutableStorageTag implements StorageTag {
	public static final MutableStorageTag INVOCATION = new MutableStorageTag();
	public static final MutableStorageTag THIS = new MutableStorageTag();
	
	private MutableStorageTag() {} // TODO: scoped borrow

	@Override
	public StorageType getType() {
		return BorrowStorageType.INSTANCE;
	}
	
	@Override
	public String toString() {
		return this == THIS ? "mutable:this" : "mutable";
	}

	@Override
	public boolean canCastTo(StorageTag other) {
		return this == other;
	}

	@Override
	public boolean canCastFrom(StorageTag other) {
		return true;
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
}
