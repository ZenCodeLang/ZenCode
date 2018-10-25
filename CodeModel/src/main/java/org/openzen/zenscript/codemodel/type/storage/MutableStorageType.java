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
public class MutableStorageType implements StorageType {
	public static final MutableStorageType INSTANCE = new MutableStorageType();

	private MutableStorageType() {}

	@Override
	public String getName() {
		return "mutable";
	}

	@Override
	public StorageTag instance(CodePosition position, String[] arguments) {
		if (arguments.length > 0) {
			if (arguments.length == 1 && arguments[0].equals("this"))
				return MutableStorageTag.THIS;
			
			return new InvalidStorageTag(position, CompileExceptionCode.INVALID_STORAGE_TYPE_ARGUMENTS, "mutable storage type doesn't take arguments");
		}
		
		return MutableStorageTag.INVOCATION;
	}
}
