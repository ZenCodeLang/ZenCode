/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class IteratorTypeID implements ITypeID {
	public final ITypeID[] iteratorTypes;
	private final IteratorTypeID normalized;
	
	public IteratorTypeID(GlobalTypeRegistry registry, ITypeID[] iteratorTypes) {
		this.iteratorTypes = iteratorTypes;
		
		normalized = isDenormalized() ? normalize(registry) : this;
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
		return registry.getIterator(normalizedTypes);
	}

	@Override
	public ITypeID getUnmodified() {
		return this;
	}

	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitIterator(this);
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
		return mapper.registry.getIterator(instanced);
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
		if (!Arrays.deepEquals(this.iteratorTypes, other.iteratorTypes)) {
			return false;
		}
		return true;
	}
}
