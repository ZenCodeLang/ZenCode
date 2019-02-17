/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageType;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaFunctionalInterfaceStorageType implements StorageType {
	public static final JavaFunctionalInterfaceStorageType INSTANCE = new JavaFunctionalInterfaceStorageType();

	private JavaFunctionalInterfaceStorageType() {}
	
	@Override
	public String getName() {
		return "JavaFunctionalInterface";
	}

	@Override
	public StorageTag instance(CodePosition position, String[] arguments) {
		throw new UnsupportedOperationException("This tag can only be instanced by java interfacing code");
	}
}
