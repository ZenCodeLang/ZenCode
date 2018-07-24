/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.List;
import java.util.Objects;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class ModifiedTypeID implements ITypeID {
	public final int modifiers;
	public final ITypeID baseType;
	private final ITypeID normalized;
	
	public ModifiedTypeID(GlobalTypeRegistry registry, int modifiers, ITypeID baseType) {
		this.modifiers = modifiers;
		this.baseType = baseType;
		
		normalized = baseType.getNormalized() == baseType ? this : registry.getModified(modifiers, baseType.getNormalized());
	}
	
	@Override
	public ITypeID getNormalized() {
		return normalized;
	}
	
	@Override
	public ITypeID instance(GenericMapper mapper) {
		return mapper.registry.getModified(modifiers, baseType.instance(mapper));
	}
	
	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitModified(this);
	}
	
	@Override
	public ITypeID getUnmodified() {
		return baseType.getUnmodified();
	}

	@Override
	public boolean isOptional() {
		return (modifiers & TypeMembers.MODIFIER_OPTIONAL) > 0;
	}
	
	public boolean isImmutable() {
		return (modifiers & TypeMembers.MODIFIER_IMMUTABLE) > 0;
	}

	@Override
	public boolean isConst() {
		return (modifiers & TypeMembers.MODIFIER_CONST) > 0;
	}
	
	@Override
	public boolean isDefinition(HighLevelDefinition definition) {
		return baseType.isDefinition(definition);
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
	public boolean isObjectType() {
		return baseType.isObjectType();
	}

	@Override
	public boolean hasDefaultValue() {
		return baseType.hasDefaultValue();
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		baseType.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 79 * hash + Objects.hashCode(this.baseType);
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
		final ModifiedTypeID other = (ModifiedTypeID) obj;
		return this.baseType == other.baseType;
	}
	
	@Override
	public String toString() {
		return "const " + baseType.toString();
	}
}
