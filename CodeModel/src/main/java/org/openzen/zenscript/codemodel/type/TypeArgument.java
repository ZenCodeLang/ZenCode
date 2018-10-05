/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.AutoStorageTag;
import org.openzen.zenscript.codemodel.type.storage.SharedStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.ValueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeArgument {
	public static Map<TypeParameter, TypeArgument> getMapping(TypeParameter[] parameters, TypeArgument[] arguments) {
		Map<TypeParameter, TypeArgument> typeArguments = new HashMap<>();
		for (int i = 0; i < parameters.length; i++)
			typeArguments.put(parameters[i], arguments[i]);
		return typeArguments;
	}
	
	public static Map<TypeParameter, TypeArgument> getSelfMapping(GlobalTypeRegistry registry, TypeParameter[] parameters) {
		Map<TypeParameter, TypeArgument> typeArguments = new HashMap<>();
		for (TypeParameter parameter : parameters)
			typeArguments.put(parameter, new TypeArgument(registry.getGeneric(parameter), parameter.storage));
		return typeArguments;
	}
	
	public static final TypeArgument[] NONE = new TypeArgument[0];
	
	public final TypeID type;
	public final StorageTag storage; // can be null
	
	public TypeArgument(TypeID type, StorageTag storage) {
		this.type = type;
		this.storage = storage;
	}
	
	public TypeArgument instance(GenericMapper mapper) {
		return type.instance(mapper, storage);
	}
	
	public TypeArgument instance(CodePosition position, GenericMapper mapper, StorageTag storage) {
		if (storage != null && this.storage != null && !storage.equals(this.storage))
			return new TypeArgument(new InvalidTypeID(position, CompileExceptionCode.INCOMPATIBLE_STORAGE_TAG, "Incompatible storage tag"), null);
		
		return type.instance(mapper, storage);
	}
	
	public TypeArgument getNormalized() {
		return type.getNormalized() == type ? this : new TypeArgument(type, storage);
	}
	
	public StoredType stored() {
		return new StoredType(type, storage == null ? AutoStorageTag.INSTANCE : storage);
	}
	
	public StoredType stored(StorageTag storage) {
		return new StoredType(type, storage);
	}
	
	public TypeArgument argument(StorageTag storage) {
		return storage == null ? this : new TypeArgument(type, storage);
	}
	
	public boolean isBasic(BasicTypeID type) {
		return this.type == type;
	}
	
	@Override
	public String toString() {
		if (storage == null)
			return type.toString();
		
		return type.toString() + "`" + storage.toString();
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 53 * hash + Objects.hashCode(this.type);
		hash = 53 * hash + Objects.hashCode(this.storage);
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
		final TypeArgument other = (TypeArgument) obj;
		return Objects.equals(this.type, other.type)
				&& Objects.equals(this.storage, other.storage);
	}
}
