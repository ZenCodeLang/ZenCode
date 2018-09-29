/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.List;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericTypeID implements TypeID {
	public final TypeParameter parameter;

	public GenericTypeID(TypeParameter parameter) {
		this.parameter = parameter;
	}
	
	public boolean matches(LocalMemberCache cache, TypeID type) {
		return parameter.matches(cache, type);
	}
	
	@Override
	public GenericTypeID getNormalizedUnstored() {
		return this;
	}
	
	@Override
	public TypeID instanceUnstored(GenericMapper mapper) {
		return mapper.map(this);
	}
	
	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitGeneric(this);
	}
	
	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitGeneric(context, this);
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
	public boolean isDestructible() {
		return false; // TODO: actually depends on the type..?
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		for (TypeParameter parameter : parameters)
			if (parameter == this.parameter)
				return true;
		
		return false;
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		if (!typeParameters.contains(parameter))
			typeParameters.add(parameter);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + parameter.hashCode();
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
		final GenericTypeID other = (GenericTypeID) obj;
		return this.parameter == other.parameter;
	}
	
	@Override
	public String toString() {
		return parameter.toString();
	}
}
