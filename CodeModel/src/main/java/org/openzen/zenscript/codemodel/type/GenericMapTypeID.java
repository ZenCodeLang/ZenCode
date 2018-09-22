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

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericMapTypeID implements ITypeID {
	public final ITypeID value;
	public final TypeParameter key;
	public final StorageTag storage;
	private final GenericMapTypeID normalized;
	private final GenericMapTypeID withoutStorage;
	
	public GenericMapTypeID(GlobalTypeRegistry registry, ITypeID value, TypeParameter key, StorageTag storage) {
		this.value = value;
		this.key = key;
		this.storage = storage;
		
		normalized = value.getNormalized() == value ? this : registry.getGenericMap(value.getNormalized(), key, storage);
		withoutStorage = storage == null ? this : registry.getGenericMap(value, key, null);
	}
	
	@Override
	public GenericMapTypeID getNormalized() {
		return normalized;
	}

	@Override
	public ITypeID getUnmodified() {
		return this;
	}

	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitGenericMap(this);
	}
	
	@Override
	public <C, R> R accept(C context, TypeVisitorWithContext<C, R> visitor) {
		return visitor.visitGenericMap(context, this);
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
	public ITypeID instance(GenericMapper mapper) {
		return mapper.registry.getGenericMap(value.instance(mapper), key, storage);
	}

	@Override
	public ITypeID withStorage(GlobalTypeRegistry registry, StorageTag storage) {
		return registry.getGenericMap(value, key, storage);
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return value.hasInferenceBlockingTypeParameters(parameters);
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
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		value.extractTypeParameters(typeParameters);
		typeParameters.remove(key);
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(value.toString());
		result.append("[<");
		result.append(key.toString());
		result.append(">]");
		return result.toString();
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + this.value.hashCode();
		hash = 97 * hash + this.key.hashCode();
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
		
		final GenericMapTypeID other = (GenericMapTypeID) obj;
		return Objects.equals(this.value, other.value)
				&& Objects.equals(this.key, other.key)
				&& Objects.equals(this.storage, other.storage);
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
