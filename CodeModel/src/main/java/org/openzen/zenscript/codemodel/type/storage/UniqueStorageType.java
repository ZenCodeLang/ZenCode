/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.storage;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class UniqueStorageType implements StorageType {
	public static final UniqueStorageType INSTANCE = new UniqueStorageType();
	
	private UniqueStorageType() {}

	@Override
	public String getName() {
		return "unique";
	}

	@Override
	public StorageTag instance(CodePosition position, String[] arguments) {
		if (arguments != null)
			throw new CompileException(position, CompileExceptionCode.INVALID_STORAGE_TYPE_ARGUMENTS, "unique storage type doesn't take arguments");
		
		return UniqueStorageTag.INSTANCE;
	}
}
