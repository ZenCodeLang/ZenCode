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
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class ModifiedTypeID implements TypeID {
	public static final int MODIFIER_OPTIONAL = 1;
	public static final int MODIFIER_CONST = 2;
	public static final int MODIFIER_IMMUTABLE = 4;
	
	public final int modifiers;
	public final TypeID baseType;
	private final TypeID normalized;
	private final GlobalTypeRegistry registry;
	
	public ModifiedTypeID(GlobalTypeRegistry registry, int modifiers, TypeID baseType) {
		this.modifiers = modifiers;
		this.baseType = baseType;
		this.registry = registry;
		
		normalized = baseType.getNormalizedUnstored() == baseType ? this : registry.getModified(modifiers, baseType.getNormalizedUnstored());
	}
	
	@Override
	public TypeID getNormalizedUnstored() {
		return normalized;
	}
	
	@Override
	public TypeID instanceUnstored(GenericMapper mapper) {
		return mapper.registry.getModified(modifiers, baseType.instanceUnstored(mapper));
	}
	
	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitModified(this);
	}
	
	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitModified(context, this);
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
	public boolean isDestructible() {
		return baseType.isDestructible();
	}
	
	@Override
	public TypeID withoutOptional() {
		return without(MODIFIER_OPTIONAL);
	}
	
	@Override
	public TypeID withoutImmutable() {
		return without(MODIFIER_IMMUTABLE);
	}
	
	@Override
	public TypeID withoutConst() {
		return without(MODIFIER_CONST);
	}
	
	private TypeID without(int modifiers) {
		int newModifiers = this.modifiers & ~modifiers;
		return newModifiers == 0 ? baseType : registry.getModified(newModifiers, baseType);
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return baseType.hasInferenceBlockingTypeParameters(parameters);
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
		hash = 79 * hash + modifiers;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ModifiedTypeID other = (ModifiedTypeID) obj;
		return this.baseType == other.baseType
				&& this.modifiers == other.modifiers;
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
	
	@Override
	public String toString(StorageTag storage) {
		StringBuilder result = new StringBuilder();
		if ((modifiers & MODIFIER_IMMUTABLE) > 0)
			result.append("immutable ");
		if ((modifiers & MODIFIER_CONST) > 0)
			result.append("const ");
		result.append(baseType.toString(storage));
		if ((modifiers & MODIFIER_OPTIONAL) > 0)
			result.append("?");
		return result.toString();
	}
}
