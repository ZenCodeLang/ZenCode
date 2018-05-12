/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericMapTypeID implements ITypeID {
	public final ITypeID value;
	public final TypeParameter[] keys;
	
	public GenericMapTypeID(ITypeID value, TypeParameter[] keys) {
		this.value = value;
		this.keys = keys;
	}

	@Override
	public ITypeID getUnmodified() {
		return this;
	}

	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitGenericMap(this);
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
		return registry.getGenericMap(value.withGenericArguments(registry, arguments), keys);
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
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(value.toString());
		result.append("[<");
		for (int i = 0; i < keys.length; i++) {
			if (i > 0)
				result.append(", ");
			result.append(keys[i].toString());
		}
		result.append(">]");
		return result.toString();
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + Objects.hashCode(this.value);
		hash = 97 * hash + Arrays.deepHashCode(this.keys);
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
		final GenericMapTypeID other = (GenericMapTypeID) obj;
		if (!Objects.equals(this.value, other.value)) {
			return false;
		}
		if (!Arrays.deepEquals(this.keys, other.keys)) {
			return false;
		}
		return true;
	}
}
