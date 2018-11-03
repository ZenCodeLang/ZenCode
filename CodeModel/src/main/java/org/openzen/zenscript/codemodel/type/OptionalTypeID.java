/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class OptionalTypeID implements TypeID {
	public final TypeID baseType;
	private final TypeID normalized;
	
	public OptionalTypeID(GlobalTypeRegistry registry, TypeID baseType) {
		this.baseType = baseType;
		
		normalized = baseType.getNormalized() == baseType ? this : registry.getOptional(baseType.getNormalized());
	}
	
	@Override
	public TypeID getNormalized() {
		return normalized;
	}
	
	@Override
	public StoredType instance(GenericMapper mapper, StorageTag storage) {
		StoredType base = baseType.instance(mapper, storage);
		return mapper.registry.getOptional(base.type).stored(StorageTag.union(mapper.position, base.getSpecifiedStorage(), storage));
	}
	
	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitOptional(this);
	}
	
	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitModified(context, this);
	}

	@Override
	public boolean isOptional() {
		return true;
	}
	
	@Override
	public boolean isValueType() {
		return baseType.isValueType();
	}
	
	@Override
	public boolean isDestructible() {
		return baseType.isDestructible();
	}
	
	@Override
	public boolean isDestructible(Set<HighLevelDefinition> scanning) {
		return baseType.isDestructible(scanning);
	}
	
	@Override
	public TypeID withoutOptional() {
		return baseType;
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return baseType.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public boolean hasDefaultValue() {
		return isOptional() || baseType.hasDefaultValue();
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		baseType.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 79 * hash + Objects.hashCode(this.baseType);
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
		final OptionalTypeID other = (OptionalTypeID) obj;
		return this.baseType == other.baseType;
	}
	
	@Override
	public String toString() {
		return baseType.toString() + "?";
	}
	
	@Override
	public String toString(StorageTag storage) {
		return baseType.toString(storage) + '?';
	}
}
