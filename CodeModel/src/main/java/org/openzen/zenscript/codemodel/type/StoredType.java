/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.storage.AutoStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.ValueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class StoredType {
	public static Map<TypeParameter, TypeID> getMapping(TypeParameter[] parameters, TypeID[] arguments) {
		Map<TypeParameter, TypeID> typeArguments = new HashMap<>();
		for (int i = 0; i < parameters.length; i++)
			typeArguments.put(parameters[i], arguments[i]);
		return typeArguments;
	}
	
	public static Map<TypeParameter, TypeID> getSelfMapping(GlobalTypeRegistry registry, TypeParameter[] parameters) {
		Map<TypeParameter, TypeID> typeArguments = new HashMap<>();
		for (TypeParameter parameter : parameters)
			typeArguments.put(parameter, registry.getGeneric(parameter));
		return typeArguments;
	}
	
	public static final StoredType[] NONE = new StoredType[0];
	
	public final TypeID type;
	private final StorageTag storage;
	
	public StoredType(TypeID type, StorageTag storage) {
		if (/*!type.isValueType() && */storage == ValueStorageTag.INSTANCE)
			throw new IllegalArgumentException("storage of a nonvalue type cannot be value");
		
		this.type = type;
		this.storage = storage;
	}
	
	public StorageTag getSpecifiedStorage() {
		return storage;
	}
	
	public StorageTag getActualStorage() {
		if (storage != null)
			return storage;
		
		return type.isValueType() ? ValueStorageTag.INSTANCE : AutoStorageTag.INSTANCE;
	}
	
	public StoredType getNormalized() {
		return type.getNormalized() == type ? this : new StoredType(type.getNormalized(), storage);
	}
	
	public TypeID getSuperType(GlobalTypeRegistry registry) {
		TypeID superType = type.getSuperType(registry);
		return superType == null ? null : superType;
	}
	
	public TypeID instance(GenericMapper mapper) {
		return type.instance(mapper);
	}
	
	public boolean isDestructible() {
		return type.isDestructible() && getActualStorage().isDestructible();
	}
	
	public boolean isDestructible(Set<HighLevelDefinition> scanning) {
		return type.isDestructible(scanning) && getActualStorage().isDestructible();
	}
	
	public boolean hasDefaultValue() {
		return type.hasDefaultValue();
	}
	
	public boolean isOptional() {
		return type.isOptional();
	}
	
	public boolean isConst() {
		return getActualStorage().isConst();
	}
	
	public boolean isImmutable() {
		return getActualStorage().isImmutable();
	}
	
	public boolean isBasic(BasicTypeID type) {
		return this.type == type;
	}
	
	public boolean isGeneric() {
		return type.isGeneric();
	}
	
	public StoredType withoutOptional() {
		return new StoredType(type.withoutOptional(), storage);
	}
	
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return type.hasInferenceBlockingTypeParameters(parameters);
	}
	
	// Infers type parameters for this type so it matches with targetType
	// returns false if that isn't possible
	public Map<TypeParameter, TypeID> inferTypeParameters(LocalMemberCache cache, TypeID targetType) {
		return type.inferTypeParameters(cache, targetType);
	}
	
	public boolean isVariant() {
		return type.isVariant();
	}
	
	public boolean isEnum() {
		return type.isEnum();
	}

	public DefinitionTypeID asDefinition() {
		return (DefinitionTypeID)type;
	}
	
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 41 * hash + Objects.hashCode(this.type);
		hash = 41 * hash + Objects.hashCode(this.storage);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		
		final TypeID other = (TypeID) obj;
		return Objects.equals(this.type, other);
	}
	
	@Override
	public String toString() {
		return storage == null ? type.toString() : type.toString(storage);
	}
}
