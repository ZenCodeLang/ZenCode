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
public class SharedStorageTag implements StorageTag {
	public static final SharedStorageTag INSTANCE = new SharedStorageTag();
	
	private SharedStorageTag() {}

	@Override
	public StorageType getType() {
		return SharedStorageType.INSTANCE;
	}
}
