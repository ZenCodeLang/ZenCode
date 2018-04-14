/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Arrays;
import java.util.Map;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class IteratorTypeID implements ITypeID {
	public final ITypeID[] iteratorTypes;
	
	public IteratorTypeID(ITypeID[] iteratorTypes) {
		this.iteratorTypes = iteratorTypes;
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
	public ITypeID withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		ITypeID[] instanced = new ITypeID[iteratorTypes.length];
		for (int i = 0; i < iteratorTypes.length; i++)
			instanced[i] = iteratorTypes[i].withGenericArguments(registry, arguments);
		return registry.getIterator(instanced);
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		for (ITypeID type : iteratorTypes)
			if (type.hasInferenceBlockingTypeParameters(parameters))
				return true;
		
		return false;
	}

	@Override
	public String toCamelCaseName() {
		return "Iterator" + (iteratorTypes.length == 1 ? "" : iteratorTypes.length);
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
