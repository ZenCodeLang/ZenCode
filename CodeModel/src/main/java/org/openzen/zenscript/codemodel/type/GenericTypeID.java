/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.List;
import java.util.Objects;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericTypeID implements ITypeID {
	public final TypeParameter parameter;
	public final StorageTag storage;
	private final GenericTypeID withoutStorage;

	public GenericTypeID(GlobalTypeRegistry registry, TypeParameter parameter, StorageTag storage) {
		this.parameter = parameter;
		this.storage = storage;
		withoutStorage = storage == null ? this : registry.getGeneric(parameter, null);
	}
	
	public boolean matches(LocalMemberCache cache, ITypeID type) {
		return parameter.matches(cache, type);
	}
	
	@Override
	public GenericTypeID getNormalized() {
		return this;
	}
	
	@Override
	public ITypeID instance(GenericMapper mapper) {
		return mapper.map(this).withStorage(mapper.registry, storage);
	}
	
	@Override
	public ITypeID withStorage(GlobalTypeRegistry registry, StorageTag storage) {
		return registry.getGeneric(parameter, storage);
	}

	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitGeneric(this);
	}
	
	@Override
	public <C, R> R accept(C context, TypeVisitorWithContext<C, R> visitor) {
		return visitor.visitGeneric(context, this);
	}
	
	@Override
	public GenericTypeID getUnmodified() {
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
	public boolean isObjectType() {
		return parameter.isObjectType();
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
		hash = 47 * hash + Objects.hashCode(storage);
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
		return this.parameter == other.parameter
				&& Objects.equals(this.storage, other.storage);
	}
	
	@Override
	public String toString() {
		return parameter.toString();
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
