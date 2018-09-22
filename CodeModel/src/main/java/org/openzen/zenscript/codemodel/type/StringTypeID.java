/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.List;
import java.util.Objects;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.AnyStorageTag;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.codemodel.type.storage.SharedStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StaticStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class StringTypeID implements ITypeID {
	public static final StringTypeID ANY = new StringTypeID(AnyStorageTag.INSTANCE);
	public static final StringTypeID STATIC = new StringTypeID(StaticStorageTag.INSTANCE);
	public static final StringTypeID UNIQUE = new StringTypeID(UniqueStorageTag.INSTANCE);
	public static final StringTypeID BORROW = new StringTypeID(BorrowStorageTag.INVOCATION);
	public static final StringTypeID SHARED = new StringTypeID(SharedStorageTag.INSTANCE);
	public static final StringTypeID NOSTORAGE = new StringTypeID(null);
	
	public final StorageTag storage;
	
	public StringTypeID(StorageTag storage) {
		this.storage = storage;
	}

	@Override
	public ITypeID getUnmodified() {
		return this;
	}

	@Override
	public ITypeID getNormalized() {
		return this;
	}

	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitString(this);
	}

	@Override
	public <C, R> R accept(C context, TypeVisitorWithContext<C, R> visitor) {
		return visitor.visitString(context, this);
	}

	@Override
	public boolean hasDefaultValue() {
		return true;
	}

	@Override
	public boolean isObjectType() {
		return true;
	}

	@Override
	public ITypeID instance(GenericMapper mapper) {
		return this;
	}
	
	@Override
	public ITypeID withStorage(GlobalTypeRegistry registry, StorageTag storage) {
		return registry.getString(storage);
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		
	}
	
	@Override
	public String toString() {
		if (storage == null)
			return "string";
		
		return "string`" + storage.toString();
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + Objects.hashCode(this.storage);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		final StringTypeID other = (StringTypeID) obj;
		return Objects.equals(this.storage, other.storage);
	}

	@Override
	public StorageTag getStorage() {
		return storage;
	}

	@Override
	public ITypeID withoutStorage() {
		return NOSTORAGE;
	}
}
