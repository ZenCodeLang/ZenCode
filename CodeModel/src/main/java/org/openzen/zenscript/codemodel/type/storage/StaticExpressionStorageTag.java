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
public class StaticExpressionStorageTag implements StorageTag {
	public static final StaticExpressionStorageTag INSTANCE = new StaticExpressionStorageTag();
	
	private StaticExpressionStorageTag() {}

	@Override
	public StorageType getType() {
		return StaticExpressionStorageType.INSTANCE;
	}

	@Override
	public boolean canCastTo(StorageTag other) {
		return false;
	}

	@Override
	public boolean canCastFrom(StorageTag other) {
		return false;
	}

	@Override
	public boolean isDestructible() {
		return false;
	}
}
