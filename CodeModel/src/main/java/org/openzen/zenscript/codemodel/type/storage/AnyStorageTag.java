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
public class AnyStorageTag implements StorageTag {
	public static final AnyStorageTag INSTANCE = new AnyStorageTag();
	
	private AnyStorageTag() {}

	@Override
	public StorageType getType() {
		return AnyStorageType.INSTANCE;
	}
}
