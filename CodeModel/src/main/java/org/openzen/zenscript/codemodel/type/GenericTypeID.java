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
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericTypeID implements TypeID {
	public final TypeParameter parameter;

	public GenericTypeID(TypeParameter parameter) {
		this.parameter = parameter;
	}
	
	public boolean matches(LocalMemberCache cache, StoredType type) {
		if (type.getSpecifiedStorage() != null && parameter.storage != null && !type.getActualStorage().equals(parameter.storage))
			return false;
		
		return parameter.matches(cache, type.type);
	}
	
	@Override
	public GenericTypeID getNormalized() {
		return this;
	}
	
	@Override
	public StoredType instance(GenericMapper mapper, StorageTag storage) {
		StoredType mapped = mapper.map(this);
		return new StoredType(mapped.type, StorageTag.union(mapper.position, mapped.getSpecifiedStorage(), storage));
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
	public boolean isValueType() {
		return false;
	}
	
	@Override
	public boolean isDestructible() {
		return false; // TODO: actually depends on the type..?
	}
	
	@Override
	public boolean isDestructible(Set<HighLevelDefinition> scanning) {
		return false; // TODO: actually depends on the type..?
	}
	
	@Override
	public boolean isGeneric() {
		return true;
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
		return this.parameter.equals(other.parameter);
	}
	
	@Override
	public String toString() {
		return parameter.toString();
	}
}
