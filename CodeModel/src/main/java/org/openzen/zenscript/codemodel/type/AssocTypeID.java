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
public class AssocTypeID implements ITypeID {
	public final ITypeID keyType;
	public final ITypeID valueType;
	public final StorageTag storage;
	private final AssocTypeID normalized;
	private final AssocTypeID withoutStorage;
	
	public AssocTypeID(GlobalTypeRegistry typeRegistry, ITypeID keyType, ITypeID valueType, StorageTag storage) {
		this.keyType = keyType;
		this.valueType = valueType;
		this.storage = storage;
		
		if (keyType != keyType.getNormalized() || valueType != valueType.getNormalized())
			normalized = typeRegistry.getAssociative(keyType.getNormalized(), valueType.getNormalized(), storage);
		else
			normalized = this;
		
		withoutStorage = storage == null ? this : typeRegistry.getAssociative(normalized.keyType, normalized.valueType, null);
	}
	
	@Override
	public AssocTypeID instance(GenericMapper mapper) {
		return mapper.registry.getAssociative(
				keyType.instance(mapper),
				valueType.instance(mapper),
				storage);
	}
	
	@Override
	public AssocTypeID withStorage(GlobalTypeRegistry registry, StorageTag storage) {
		return registry.getAssociative(keyType, valueType, storage);
	}
	
	@Override
	public AssocTypeID getNormalized() {
		return normalized;
	}
	
	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitAssoc(this);
	}
	
	@Override
	public <C, R> R accept(C context, TypeVisitorWithContext<C, R> visitor) {
		return visitor.visitAssoc(context, this);
	}
	
	@Override
	public AssocTypeID getUnmodified() {
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
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return keyType.hasInferenceBlockingTypeParameters(parameters) || valueType.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public boolean hasDefaultValue() {
		return true;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		keyType.extractTypeParameters(typeParameters);
		valueType.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + keyType.hashCode();
		hash = 29 * hash + valueType.hashCode();
		hash = 29 * hash + Objects.hashCode(storage);
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
		final AssocTypeID other = (AssocTypeID) obj;
		return this.keyType == other.keyType
				&& this.valueType == other.valueType
				&& Objects.equals(this.storage, other.storage);
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(valueType.toString());
		result.append('[');
		result.append(keyType.toString());
		result.append(']');
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
