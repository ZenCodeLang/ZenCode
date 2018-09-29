/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.storage;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class StaticStorageType implements StorageType {
	public static final StaticStorageType INSTANCE = new StaticStorageType();
	
	private StaticStorageType() {}

	@Override
	public String getName() {
		return "static";
	}

	@Override
	public StorageTag instance(CodePosition position, String[] arguments) {
		if (arguments != null)
			return new InvalidStorageTag(position, CompileExceptionCode.INVALID_STORAGE_TYPE_ARGUMENTS, "static storage type doesn't take arguments");
		
		return StaticStorageTag.INSTANCE;
	}
}
