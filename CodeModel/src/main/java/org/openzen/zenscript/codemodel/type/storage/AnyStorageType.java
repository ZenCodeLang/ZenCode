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
public class AnyStorageType implements StorageType {
	public static final AnyStorageType INSTANCE = new AnyStorageType();
	
	private AnyStorageType() {}

	@Override
	public String getName() {
		return "any";
	}

	@Override
	public StorageTag instance(CodePosition position, String[] arguments) {
		if (arguments != null)
			return new InvalidStorageTag(position, CompileExceptionCode.INVALID_STORAGE_TYPE_ARGUMENTS, "any storage type doesn't take arguments");
		
		return AnyStorageTag.INSTANCE;
	}
}
