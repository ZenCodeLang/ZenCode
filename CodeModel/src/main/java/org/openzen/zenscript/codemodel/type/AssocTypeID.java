/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.List;
import java.util.Set;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class AssocTypeID implements TypeID {
	public final StoredType keyType;
	public final StoredType valueType;
	private final AssocTypeID normalized;
	
	public AssocTypeID(GlobalTypeRegistry typeRegistry, StoredType keyType, StoredType valueType) {
		this.keyType = keyType;
		this.valueType = valueType;
		
		if (keyType != keyType.getNormalized() || valueType != valueType.getNormalized())
			normalized = typeRegistry.getAssociative(keyType.getNormalized(), valueType.getNormalized());
		else
			normalized = this;
	}
	
	@Override
	public StoredType instance(GenericMapper mapper, StorageTag storage) {
		return mapper.registry.getAssociative(
				keyType.instance(mapper),
				valueType.instance(mapper)).stored(storage);
	}
	
	@Override
	public AssocTypeID getNormalized() {
		return normalized;
	}
	
	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitAssoc(this);
	}
	
	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitAssoc(context, this);
	}

	@Override
	public boolean isOptional() {
		return false;
	}
	
	@Override
	public boolean isValueType() {
		return false;
	}
	
	@Override
	public boolean isDestructible() {
		return keyType.isDestructible() || valueType.isDestructible();
	}
	
	@Override
	public boolean isDestructible(Set<HighLevelDefinition> scanning) {
		return keyType.isDestructible(scanning) || valueType.isDestructible(scanning);
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
		keyType.type.extractTypeParameters(typeParameters);
		valueType.type.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + keyType.hashCode();
		hash = 29 * hash + valueType.hashCode();
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
		return this.keyType.equals(other.keyType)
				&& this.valueType.equals(other.valueType);
	}
	
	@Override
	public String toString() {
		return valueType.toString() + '[' + keyType.toString() + ']';
	}
}
