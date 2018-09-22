/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class IteratorTypeID implements ITypeID {
	public final ITypeID[] iteratorTypes;
	public final StorageTag storage;
	private final IteratorTypeID normalized;
	private final IteratorTypeID withoutStorage;
	
	public IteratorTypeID(GlobalTypeRegistry registry, ITypeID[] iteratorTypes, StorageTag storage) {
		this.iteratorTypes = iteratorTypes;
		this.storage = storage;
		
		normalized = isDenormalized() ? normalize(registry) : this;
		withoutStorage = storage == null ? this : registry.getIterator(iteratorTypes, null);
	}
	
	@Override
	public IteratorTypeID getNormalized() {
		return normalized;
	}
	
	private boolean isDenormalized() {
		for (ITypeID type : iteratorTypes)
			if (type.getNormalized() != type)
				return true;
		
		return false;
	}
	
	private IteratorTypeID normalize(GlobalTypeRegistry registry) {
		ITypeID[] normalizedTypes = new ITypeID[iteratorTypes.length];
		for (int i = 0; i < normalizedTypes.length; i++)
			normalizedTypes[i] = iteratorTypes[i].getNormalized();
		return registry.getIterator(normalizedTypes, storage);
	}

	@Override
	public ITypeID getUnmodified() {
		return this;
	}

	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitIterator(this);
	}
	
	@Override
	public <C, R> R accept(C context, TypeVisitorWithContext<C, R> visitor) {
		return visitor.visitIterator(context, this);
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
	public ITypeID instance(GenericMapper mapper) {
		ITypeID[] instanced = new ITypeID[iteratorTypes.length];
		for (int i = 0; i < iteratorTypes.length; i++)
			instanced[i] = iteratorTypes[i].instance(mapper);
		return mapper.registry.getIterator(instanced, storage);
	}
	
	@Override
	public ITypeID withStorage(GlobalTypeRegistry registry, StorageTag storage) {
		return registry.getIterator(iteratorTypes, storage);
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		for (ITypeID type : iteratorTypes)
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
		for (ITypeID type : iteratorTypes)
			type.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 13 * hash + Arrays.deepHashCode(this.iteratorTypes);
		hash = 13 * hash + Objects.hashCode(storage);
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
		return Arrays.deepEquals(this.iteratorTypes, other.iteratorTypes)
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
