/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionTypeID implements TypeID {
	public static DefinitionTypeID forType(GlobalTypeRegistry registry, HighLevelDefinition definition) {
		if (definition.typeParameters != null && definition.typeParameters.length > 0)
			throw new IllegalArgumentException("Definition has type arguments!");
		
		return new DefinitionTypeID(registry, definition, TypeID.NONE);
	}
	
	public final HighLevelDefinition definition;
	public final TypeID[] typeArguments;
	public final DefinitionTypeID outer;
	private TypeID normalized;
	
	public TypeID superType;
	
	public DefinitionTypeID(GlobalTypeRegistry typeRegistry, HighLevelDefinition definition, TypeID[] typeArguments) {
		this(typeRegistry, definition, typeArguments, null);
	}
	
	// For inner classes of generic outer classes
	public DefinitionTypeID(GlobalTypeRegistry typeRegistry, HighLevelDefinition definition, TypeID[] typeArguments, DefinitionTypeID outer) {
		if (typeArguments == null)
			throw new NullPointerException("typeParameters cannot be null");
		if (typeArguments.length != definition.getNumberOfGenericParameters())
			throw new IllegalArgumentException("Wrong number of type parameters!");
		if (definition.isInnerDefinition() && !definition.isStatic() && outer == null)
			throw new IllegalArgumentException("Inner definition requires outer instance");
		if ((!definition.isInnerDefinition() || definition.isStatic()) && outer != null)
			throw new IllegalArgumentException("Static inner definition must not have outer instance");
		
		this.definition = definition;
		this.typeArguments = typeArguments;
		this.outer = outer;
		
		normalized = isDenormalized() ? normalize(typeRegistry) : this;
		if (normalized instanceof DefinitionTypeID && ((DefinitionTypeID)normalized).isDenormalized())
			throw new AssertionError();
	}
	
	private boolean isDenormalized() {
		if (definition instanceof AliasDefinition)
			return true;
		
		for (TypeID typeParameter : typeArguments)
			if (typeParameter.getNormalizedUnstored() != typeParameter)
				return true;
		if (outer != null && outer.getNormalizedUnstored() != outer)
			return true;
		
		return false;
	}
	
	private TypeID normalize(GlobalTypeRegistry typeRegistry) {
		if (definition instanceof AliasDefinition) {
			AliasDefinition alias = (AliasDefinition)definition;
			if (alias.type == null)
				throw new IllegalStateException("Alias type not yet initialized!");
			
			Map<TypeParameter, TypeID> typeMapping = new HashMap<>();
			for (int i = 0; i < definition.typeParameters.length; i++)
				typeMapping.put(definition.typeParameters[i], typeArguments[i].getNormalizedUnstored());
			GenericMapper mapper = new GenericMapper(typeRegistry, typeMapping);
			TypeID result = alias.type.instanceUnstored(mapper).getNormalizedUnstored();
			return result;
		}
		
		TypeID[] normalizedTypeParameters = new TypeID[typeArguments.length];
		for (int i = 0; i < normalizedTypeParameters.length; i++)
			normalizedTypeParameters[i] = typeArguments[i].getNormalizedUnstored();
		
		return typeRegistry.getForDefinition(definition, normalizedTypeParameters, outer == null ? null : (DefinitionTypeID)outer.getNormalizedUnstored());
	}
	
	public boolean hasTypeParameters() {
		return typeArguments.length > 0;
	}
	
	public Map<TypeParameter, TypeID> getTypeParameterMapping() {
		Map<TypeParameter, TypeID> mapping = new HashMap<>();
		DefinitionTypeID current = this;
		do {
			if (current.typeArguments != null) {
				if (current.definition.typeParameters == null)
					System.out.println("Type parameters but no generic parameters");
				else
					for (int i = 0; i < current.typeArguments.length; i++)
						mapping.put(current.definition.typeParameters[i], current.typeArguments[i]);
			}

			current = current.outer;
		} while (current != null && !current.definition.isStatic());
		return mapping;
	}
	
	public DefinitionTypeID(HighLevelDefinition definition) {
		this.definition = definition;
		this.typeArguments = TypeID.NONE;
		this.superType = definition.getSuperType();
		this.outer = null;
	}
	
	@Override
	public TypeID getNormalizedUnstored() {
		return normalized;
	}
	
	@Override
	public DefinitionTypeID instanceUnstored(GenericMapper mapper) {
		if (!hasTypeParameters() && outer == null)
			return this;
		if (mapper.getMapping().isEmpty())
			return this;
		if (mapper.registry == null)
			throw new NullPointerException();
		
		TypeID[] instancedArguments = TypeID.NONE;
		if (hasTypeParameters()) {
			instancedArguments = new TypeID[typeArguments.length];
			for (int i = 0; i < typeArguments.length; i++)
				instancedArguments[i] = typeArguments[i].instanceUnstored(mapper);
		}
		
		DefinitionTypeID instancedOuter = outer == null ? null : outer.instanceUnstored(mapper);
		return mapper.registry.getForDefinition(definition, instancedArguments, instancedOuter);
	}
	
	@Override
	public TypeID getSuperType(GlobalTypeRegistry registry) {
		return definition.getSuperType() == null ? null : definition.getSuperType().instanceUnstored(new GenericMapper(registry, getTypeParameterMapping()));
	}
	
	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitDefinition(this);
	}
	
	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitDefinition(context, this);
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
		return definition instanceof StructDefinition || definition instanceof EnumDefinition;
	}

	@Override
	public boolean isEnum() {
		return definition instanceof EnumDefinition;
	}
	
	@Override
	public boolean isVariant() {
		return definition instanceof VariantDefinition;
	}
	
	@Override
	public boolean isDestructible() {
		return definition.isDestructible();
	}
	
	@Override
	public boolean isDestructible(Set<HighLevelDefinition> scanning) {
		return definition.isDestructible(scanning);
	}
	
	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		if (hasTypeParameters()) {
			for (TypeID typeParameter : typeArguments)
				if (typeParameter.hasInferenceBlockingTypeParameters(parameters))
					return true;
		}
		
		return superType != null && superType.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + definition.hashCode();
		hash = 97 * hash + Arrays.deepHashCode(typeArguments);
		hash = 97 * hash + Objects.hashCode(outer);
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
		final DefinitionTypeID other = (DefinitionTypeID) obj;
		return this.definition == other.definition
				&& Arrays.deepEquals(this.typeArguments, other.typeArguments)
				&& Objects.equals(outer, this.outer);
	}
	
	@Override
	public String toString() {
		if (!hasTypeParameters() && outer == null)
			return definition.name;
		
		StringBuilder result = new StringBuilder();
		if (outer != null)
			result.append(outer.toString()).append('.');
		
		result.append(definition.name);
		result.append('<');
		for (int i = 0; i < typeArguments.length; i++) { 
			if (i > 0)
				result.append(", ");
			result.append(typeArguments[i].toString());
		}
		result.append('>');
		return result.toString();
	}

	@Override
	public boolean hasDefaultValue() {
		return definition.hasEmptyConstructor();
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		for (TypeID type : this.typeArguments)
			type.extractTypeParameters(typeParameters);
	}

	public DefinitionTypeID getInnerType(GenericName name, GlobalTypeRegistry registry) {
		HighLevelDefinition type = definition.getInnerType(name.name);
		return registry.getForDefinition(type, name.arguments, this);
	}
}
