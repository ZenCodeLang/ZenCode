/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Map;
import java.util.Objects;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class OptionalTypeID implements ITypeID {
	public final ITypeID baseType;
	
	public OptionalTypeID(ITypeID baseType) {
		this.baseType = baseType;
	}
	
	@Override
	public ITypeID withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		return registry.getModified(TypeMembers.MODIFIER_OPTIONAL, baseType.withGenericArguments(registry, arguments));
	}

	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitOptional(this);
	}
	
	@Override
	public ITypeID getUnmodified() {
		return baseType.getUnmodified();
	}

	@Override
	public boolean isOptional() {
		return true;
	}
	
	@Override
	public ITypeID getOptionalBase() {
		return baseType;
	}

	@Override
	public boolean isConst() {
		return false;
	}
	
	@Override
	public ITypeID unwrap() {
		return baseType;
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return baseType.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public String toCamelCaseName() {
		return "Optional" + baseType.toCamelCaseName();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + Objects.hashCode(this.baseType);
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
		final OptionalTypeID other = (OptionalTypeID) obj;
		return this.baseType == other.baseType;
	}
	
	@Override
	public String toString() {
		return baseType.toString() + "?";
	}
}
