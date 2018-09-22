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
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class ArrayTypeID implements ITypeID {
	public static final ArrayTypeID INT_NOSTORAGE = new ArrayTypeID(BasicTypeID.INT, 1, null);
	public static final ArrayTypeID CHAR_NOSTORAGE = new ArrayTypeID(BasicTypeID.CHAR, 1, null);
	
	public static final ArrayTypeID INT_UNIQUE = new ArrayTypeID(BasicTypeID.INT, 1, UniqueStorageTag.INSTANCE);
	public static final ArrayTypeID CHAR_UNIQUE = new ArrayTypeID(BasicTypeID.CHAR, 1, UniqueStorageTag.INSTANCE);
	
	public final ITypeID elementType;
	public final int dimension;
	public final StorageTag storage;
	private final ArrayTypeID normalized;
	private final ArrayTypeID withoutStorage;

	private ArrayTypeID(ITypeID elementType, int dimension, StorageTag storage) {
		this.elementType = elementType;
		this.dimension = dimension;
		this.normalized = this;
		this.storage = storage;
		
		if (storage == null) {
			withoutStorage = this;
		} else {
			if (elementType == BasicTypeID.INT)
				withoutStorage = INT_NOSTORAGE;
			else if (elementType == BasicTypeID.CHAR)
				withoutStorage = CHAR_NOSTORAGE;
			else
				throw new IllegalArgumentException();
		}
	}
	
	public ArrayTypeID(GlobalTypeRegistry registry, ITypeID elementType, int dimension, StorageTag storage) {
		this.elementType = elementType;
		this.dimension = dimension;
		this.normalized = elementType.getNormalized() == elementType ? this : registry.getArray(elementType.getNormalized(), dimension, storage);
		this.storage = storage;
		
		withoutStorage = storage == null ? this : registry.getArray(elementType, dimension, null);
	}
	
	@Override
	public ArrayTypeID getNormalized() {
		return normalized;
	}
	
	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitArray(this);
	}
	
	@Override
	public <C, R> R accept(C context, TypeVisitorWithContext<C, R> visitor) {
		return visitor.visitArray(context, this);
	}
	
	@Override
	public ArrayTypeID getUnmodified() {
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
		return true;
	}
	
	@Override
	public ArrayTypeID instance(GenericMapper mapper) {
		return mapper.registry.getArray(elementType.instance(mapper), dimension, storage);
	}
	
	@Override
	public ArrayTypeID withStorage(GlobalTypeRegistry registry, StorageTag storage) {
		return registry.getArray(elementType, dimension, storage);
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return elementType.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		elementType.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + elementType.hashCode();
		hash = 79 * hash + dimension;
		hash = 79 * hash + Objects.hashCode(storage);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ArrayTypeID other = (ArrayTypeID) obj;
		return this.dimension == other.dimension
				&& this.elementType == other.elementType
				&& Objects.equals(this.storage, other.storage);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(elementType.toString());
		result.append('[');
		for (int i = 1; i < dimension; i++) {
			result.append(',');
		}
		result.append(']');
		if (storage != null) {
			result.append('`');
			result.append(storage.toString());
		}
		return result.toString();
	}

	@Override
	public StorageTag getStorage() {
		return storage;
	}

	@Override
	public ITypeID withoutStorage() {
		return withoutStorage;
	}
}
