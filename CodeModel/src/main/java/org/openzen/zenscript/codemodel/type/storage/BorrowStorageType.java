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
public class BorrowStorageType implements StorageType {
	public static final BorrowStorageType INSTANCE = new BorrowStorageType();

	private BorrowStorageType() {}

	@Override
	public String getName() {
		return "borrow";
	}

	@Override
	public StorageTag instance(CodePosition position, String[] arguments) {
		if (arguments != null) {
			if (arguments.length == 1 && arguments[0].equals("this"))
				return BorrowStorageTag.THIS;
			
			throw new CompileException(position, CompileExceptionCode.INVALID_STORAGE_TYPE_ARGUMENTS, "borrow storage type doesn't take arguments");
		}
		
		return BorrowStorageTag.INVOCATION;
	}
}
