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
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.ValueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class RangeTypeID implements TypeID {
	public static final RangeTypeID INT = new RangeTypeID(null, BasicTypeID.INT.stored);
	public static final RangeTypeID USIZE = new RangeTypeID(null, BasicTypeID.USIZE.stored);
	
	public final StoredType baseType;
	private final RangeTypeID normalized;
	public final StoredType stored;
	
	public RangeTypeID(GlobalTypeRegistry registry, StoredType baseType) {
		this.baseType = baseType;
		
		if (baseType.getNormalized().equals(baseType)) {
			normalized = this;
		} else {
			normalized = registry.getRange(baseType.getNormalized());
		}
		
		stored = new StoredType(this, baseType.storage);
	}
	
	@Override
	public RangeTypeID getNormalized() {
		return normalized;
	}
	
	@Override
	public TypeArgument instance(GenericMapper mapper, StorageTag storage) {
		return mapper.registry.getRange(baseType.instance(mapper)).argument(storage);
	}
	
	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitRange(this);
	}
	
	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitRange(context, this);
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
	public boolean isValueType() {
		return baseType.type.isValueType();
	}
	
	@Override
	public boolean isDestructible() {
		return baseType.isDestructible();
	}
	
	@Override
	public boolean isDestructible(Set<HighLevelDefinition> scanning) {
		return baseType.isDestructible(scanning);
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
		baseType.type.extractTypeParameters(typeParameters);
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
		return this.baseType.equals(other.baseType);
	}
	
	@Override
	public String toString() {
		return baseType.toString() + " .. " + baseType.toString();
	}
}
