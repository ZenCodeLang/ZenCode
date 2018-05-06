/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Map;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class ArrayTypeID implements ITypeID {
	public final ITypeID elementType;
	public final int dimension;

	public ArrayTypeID(ITypeID elementType, int dimension) {
		this.elementType = elementType;
		this.dimension = dimension;
	}
	
	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitArray(this);
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
	public ArrayTypeID withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		return registry.getArray(elementType.withGenericArguments(registry, arguments), dimension);
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return elementType.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + elementType.hashCode();
		hash = 79 * hash + dimension;
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
		return this.dimension == other.dimension && this.elementType == other.elementType;
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
		return result.toString();
	}
}
