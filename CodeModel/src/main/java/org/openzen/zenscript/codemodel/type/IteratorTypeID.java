/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Arrays;
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
public class IteratorTypeID implements TypeID {
	public final StoredType[] iteratorTypes;
	private final IteratorTypeID normalized;
	
	public IteratorTypeID(GlobalTypeRegistry registry, StoredType[] iteratorTypes) {
		this.iteratorTypes = iteratorTypes;
		
		normalized = isDenormalized() ? normalize(registry) : this;
	}
	
	@Override
	public IteratorTypeID getNormalized() {
		return normalized;
	}
	
	private boolean isDenormalized() {
		for (StoredType type : iteratorTypes)
			if (type.getNormalized() != type)
				return true;
		
		return false;
	}
	
	private IteratorTypeID normalize(GlobalTypeRegistry registry) {
		StoredType[] normalizedTypes = new StoredType[iteratorTypes.length];
		for (int i = 0; i < normalizedTypes.length; i++)
			normalizedTypes[i] = iteratorTypes[i].getNormalized();
		return registry.getIterator(normalizedTypes);
	}
	
	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitIterator(this);
	}
	
	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitIterator(context, this);
	}

	@Override
	public boolean isOptional() {
		return false;
	}
	
	@Override
	public boolean isDestructible() {
		return false;
	}
	
	@Override
	public boolean isDestructible(Set<HighLevelDefinition> scanning) {
		return false;
	}
	
	@Override
	public boolean isValueType() {
		return false;
	}
	
	@Override
	public StoredType instance(GenericMapper mapper, StorageTag storage) {
		StoredType[] instanced = mapper.map(iteratorTypes);
		return mapper.registry.getIterator(instanced).stored(storage);
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		for (StoredType type : iteratorTypes)
			if (type.hasInferenceBlockingTypeParameters(parameters))
				return true;
		
		return false;
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		for (StoredType type : iteratorTypes)
			type.type.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 13 * hash + Arrays.deepHashCode(this.iteratorTypes);
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
		final IteratorTypeID other = (IteratorTypeID) obj;
		return Arrays.deepEquals(this.iteratorTypes, other.iteratorTypes);
	}
}
