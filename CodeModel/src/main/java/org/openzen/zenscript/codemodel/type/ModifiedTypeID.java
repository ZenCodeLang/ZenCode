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

/**
 *
 * @author Hoofdgebruiker
 */
public class ModifiedTypeID implements ITypeID {
	public static final int MODIFIER_OPTIONAL = 1;
	public static final int MODIFIER_CONST = 2;
	public static final int MODIFIER_IMMUTABLE = 4;
	
	public final int modifiers;
	public final ITypeID baseType;
	private final ITypeID normalized;
	private final GlobalTypeRegistry registry;
	
	public ModifiedTypeID(GlobalTypeRegistry registry, int modifiers, ITypeID baseType) {
		this.modifiers = modifiers;
		this.baseType = baseType;
		this.registry = registry;
		
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
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitModified(this);
	}
	
	@Override
	public <C, R> R accept(C context, TypeVisitorWithContext<C, R> visitor) {
		return visitor.visitModified(context, this);
	}
	
	@Override
	public ITypeID getUnmodified() {
		return baseType.getUnmodified();
	}

	@Override
	public boolean isOptional() {
		return (modifiers & MODIFIER_OPTIONAL) > 0;
	}
	
	@Override
	public boolean isImmutable() {
		return (modifiers & MODIFIER_IMMUTABLE) > 0;
	}

	@Override
	public boolean isConst() {
		return (modifiers & MODIFIER_CONST) > 0;
	}
	
	@Override
	public ITypeID withoutOptional() {
		return without(MODIFIER_OPTIONAL);
	}
	
	@Override
	public ITypeID withoutImmutable() {
		return without(MODIFIER_IMMUTABLE);
	}
	
	@Override
	public ITypeID withoutConst() {
		return without(MODIFIER_CONST);
	}
	
	@Override
	public boolean isDefinition(HighLevelDefinition definition) {
		return baseType.isDefinition(definition);
	}
	
	private ITypeID without(int modifiers) {
		int newModifiers = this.modifiers & ~modifiers;
		return newModifiers == 0 ? baseType : registry.getModified(newModifiers, baseType);
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
		return isOptional() || baseType.hasDefaultValue();
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
		StringBuilder result = new StringBuilder();
		if ((modifiers & MODIFIER_IMMUTABLE) > 0)
			result.append("immutable ");
		if ((modifiers & MODIFIER_CONST) > 0)
			result.append("const ");
		result.append(baseType.toString());
		if ((modifiers & MODIFIER_OPTIONAL) > 0)
			result.append("?");
		return result.toString();
	}
}
