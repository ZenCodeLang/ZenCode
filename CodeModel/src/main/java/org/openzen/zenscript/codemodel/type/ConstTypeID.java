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
public class ConstTypeID implements ITypeID {
	public final ITypeID baseType;
	
	public ConstTypeID(ITypeID baseType) {
		this.baseType = baseType;
	}
	
	@Override
	public ITypeID instance(GenericMapper mapper) {
		return mapper.registry.getModified(TypeMembers.MODIFIER_CONST, baseType.instance(mapper));
	}
	
	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitConst(this);
	}
	
	@Override
	public ITypeID getUnmodified() {
		return baseType.getUnmodified();
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isConst() {
		return true;
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
		final ConstTypeID other = (ConstTypeID) obj;
		return this.baseType == other.baseType;
	}
	
	@Override
	public String toString() {
		return "const " + baseType.toString();
	}
}
