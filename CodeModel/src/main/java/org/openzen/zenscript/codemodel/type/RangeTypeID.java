/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.List;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.ValueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class RangeTypeID implements ITypeID {
	public static final RangeTypeID INT = new RangeTypeID(null, BasicTypeID.INT);
	public static final RangeTypeID USIZE = new RangeTypeID(null, BasicTypeID.USIZE);
	
	public final ITypeID baseType;
	private final RangeTypeID normalized;
	
	public RangeTypeID(GlobalTypeRegistry registry, ITypeID baseType) {
		this.baseType = baseType;
		
		if (baseType.getNormalized() == baseType) {
			normalized = this;
		} else {
			normalized = registry.getRange(baseType.getNormalized());
		}
	}
	
	@Override
	public RangeTypeID getNormalized() {
		return normalized;
	}
	
	@Override
	public ITypeID instance(GenericMapper mapper) {
		return mapper.registry.getRange(baseType.instance(mapper));
	}
	
	@Override
	public ITypeID withStorage(GlobalTypeRegistry registry, StorageTag storage) {
		return registry.getRange(baseType.withStorage(registry, storage));
	}

	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitRange(this);
	}
	
	@Override
	public <C, R> R accept(C context, TypeVisitorWithContext<C, R> visitor) {
		return visitor.visitRange(context, this);
	}
	
	@Override
	public RangeTypeID getUnmodified() {
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
		return false;
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return baseType.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		baseType.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 89 * hash + baseType.hashCode();
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
		final RangeTypeID other = (RangeTypeID) obj;
		return this.baseType == other.baseType;
	}
	
	@Override
	public String toString() {
		return baseType.toString() + " .. " + baseType.toString();
	}

	@Override
	public StorageTag getStorage() {
		return ValueStorageTag.INSTANCE;
	}

	@Override
	public ITypeID withoutStorage() {
		return this;
	}
}
