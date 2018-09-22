/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.ValueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public enum BasicTypeID implements ITypeID {
	VOID("void"),
	NULL("null"),
	BOOL("bool"),
	BYTE("byte"),
	SBYTE("sbyte"),
	SHORT("short"),
	USHORT("ushort"),
	INT("int"),
	UINT("uint"),
	LONG("long"),
	ULONG("ulong"),
	USIZE("usize"),
	FLOAT("float"),
	DOUBLE("double"),
	CHAR("char"),
	
	UNDETERMINED("undetermined");
	
	public static final List<ITypeID> HINT_BOOL = Collections.singletonList(BOOL);
	
	public final String name;
	
	BasicTypeID(String name) {
		this.name = name;
	}
	
	@Override
	public BasicTypeID getNormalized() {
		return this;
	}
	
	@Override
	public ITypeID instance(GenericMapper mapper) {
		return this;
	}

	@Override
	public ITypeID withStorage(GlobalTypeRegistry registry, StorageTag storage) {
		return this;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitBasic(this);
	}
	
	@Override
	public <C, R> R accept(C context, TypeVisitorWithContext<C, R> visitor) {
		return visitor.visitBasic(context, this);
	}
	
	@Override
	public BasicTypeID getUnmodified() {
		return this;
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isConst() {
		return false;
	}
	
	@Override
	public boolean isObjectType() {
		return false;
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return false;
	}

	@Override
	public boolean hasDefaultValue() {
		return true;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		
	}

	@Override
	public StorageTag getStorage() {
		return ValueStorageTag.INSTANCE;
	}

	@Override
	public ITypeID withoutStorage() {
		return this;
	}
}
