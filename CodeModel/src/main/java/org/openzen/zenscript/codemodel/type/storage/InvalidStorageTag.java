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
public class InvalidStorageTag implements StorageTag {
	public final CodePosition position;
	public final CompileExceptionCode code;
	public final String message;
	
	public InvalidStorageTag(CodePosition position, CompileExceptionCode code, String message) {
		this.position = position;
		this.code = code;
		this.message = message;
	}

	@Override
	public StorageType getType() {
		return InvalidStorageType.INSTANCE;
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
	
	@Override
	public String toString() {
		return "invalid";
	}
}
