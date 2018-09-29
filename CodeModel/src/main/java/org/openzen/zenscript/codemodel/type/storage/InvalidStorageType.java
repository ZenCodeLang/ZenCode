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
public class InvalidStorageType implements StorageType {
	public static final InvalidStorageType INSTANCE = new InvalidStorageType();

	@Override
	public String getName() {
		return "INVALID";
	}

	@Override
	public StorageTag instance(CodePosition position, String[] arguments) {
		throw new UnsupportedOperationException();
	}
}
