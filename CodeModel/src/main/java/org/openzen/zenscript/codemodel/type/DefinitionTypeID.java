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
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionTypeID implements ITypeID {
	public static DefinitionTypeID forType(HighLevelDefinition definition) {
		if (definition.genericParameters != null && definition.genericParameters.length > 0)
			throw new IllegalArgumentException("Definition has type arguments!");
		
		return new DefinitionTypeID(null, definition, ITypeID.NONE);
	}
	
	public final HighLevelDefinition definition;
	public final ITypeID[] typeParameters;
	public final DefinitionTypeID outer;
	private ITypeID normalized;
	
	public ITypeID superType;
	
	public DefinitionTypeID(GlobalTypeRegistry typeRegistry, HighLevelDefinition definition, ITypeID[] typeParameters) {
		this(typeRegistry, definition, typeParameters, null);
	}
	
	// For inner classes of generic outer classes
	public DefinitionTypeID(GlobalTypeRegistry typeRegistry, HighLevelDefinition definition, ITypeID[] typeParameters, DefinitionTypeID outer) {
		if (typeParameters == null)
			throw new NullPointerException("typeParameters cannot be null");
		if (typeParameters.length != definition.getNumberOfGenericParameters())
			throw new IllegalArgumentException("Wrong number of type parameters!");
		if (definition.isInnerDefinition() && !definition.isStatic() && outer == null)
			throw new IllegalArgumentException("Inner definition requires outer instance");
		if ((!definition.isInnerDefinition() || definition.isStatic()) && outer != null)
			throw new IllegalArgumentException("Static inner definition must not have outer instance");
		
		this.definition = definition;
		this.typeParameters = typeParameters;
		this.outer = outer;
		
		normalized = isDenormalized() ? normalize(typeRegistry) : this;
		if (normalized instanceof DefinitionTypeID && ((DefinitionTypeID)normalized).isDenormalized())
			throw new AssertionError();
	}
	
	private boolean isDenormalized() {
		if (definition instanceof AliasDefinition)
			return true;
		
		for (ITypeID typeParameter : typeParameters)
			if (typeParameter.getNormalized() != typeParameter)
				return true;
		if (outer != null && outer.getNormalized() != outer)
			return true;
		
		return false;
	}
	
	private ITypeID normalize(GlobalTypeRegistry typeRegistry) {
		if (definition instanceof AliasDefinition) {
			AliasDefinition alias = (AliasDefinition)definition;
			if (alias.type == null)
				throw new IllegalStateException("Alias type not yet initialized!");
			
			Map<TypeParameter, ITypeID> typeMapping = new HashMap<>();
			for (int i = 0; i < definition.genericParameters.length; i++)
				typeMapping.put(definition.genericParameters[i], typeParameters[i].getNormalized());
			GenericMapper mapper = new GenericMapper(typeRegistry, typeMapping);
			ITypeID result = alias.type.instance(mapper).getNormalized();
			return result;
		}
		
		ITypeID[] normalizedTypeParameters = new ITypeID[typeParameters.length];
		for (int i = 0; i < normalizedTypeParameters.length; i++)
			normalizedTypeParameters[i] = typeParameters[i].getNormalized();
		
		return typeRegistry.getForDefinition(definition, normalizedTypeParameters, outer == null ? null : (DefinitionTypeID)outer.getNormalized());
	}
	
	public boolean hasTypeParameters() {
		return typeParameters.length > 0;
	}
	
	public Map<TypeParameter, ITypeID> getTypeParameterMapping() {
		Map<TypeParameter, ITypeID> mapping = new HashMap<>();
		DefinitionTypeID current = this;
		do {
			if (current.typeParameters != null) {
				if (current.definition.genericParameters == null)
					System.out.println("Type parameters but no generic parameters");
				else
					for (int i = 0; i < current.typeParameters.length; i++)
						mapping.put(current.definition.genericParameters[i], current.typeParameters[i]);
			}

			current = current.outer;
		} while (current != null && !current.definition.isStatic());
		return mapping;
	}
	
	public DefinitionTypeID(HighLevelDefinition definition) {
		this.definition = definition;
		this.typeParameters = ITypeID.NONE;
		this.superType = definition.getSuperType();
		this.outer = null;
	}
	
	@Override
	public ITypeID getNormalized() {
		return normalized;
	}
	
	@Override
	public DefinitionTypeID instance(GenericMapper mapper) {
		if (!hasTypeParameters() && outer == null)
			return this;
		if (mapper.getMapping().isEmpty())
			return this;
		if (mapper.registry == null)
			throw new NullPointerException();
		
		ITypeID[] instancedArguments = ITypeID.NONE;
		if (hasTypeParameters()) {
			instancedArguments = new ITypeID[typeParameters.length];
			for (int i = 0; i < typeParameters.length; i++) {
				instancedArguments[i] = typeParameters[i].instance(mapper);
			}
		}
		
		DefinitionTypeID instancedOuter = outer == null ? null : outer.instance(mapper);
		return mapper.registry.getForDefinition(definition, instancedArguments, instancedOuter);
	}
	
	@Override
	public ITypeID getSuperType(GlobalTypeRegistry registry) {
		return definition.getSuperType() == null ? null : definition.getSuperType().instance(new GenericMapper(registry, getTypeParameterMapping()));
	}
	
	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitDefinition(this);
	}
	
	@Override
	public DefinitionTypeID getUnmodified() {
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
		return true;
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
	public boolean isDefinition(HighLevelDefinition definition) {
		return definition == this.definition;
	}
	
	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		if (hasTypeParameters()) {
			for (ITypeID typeParameter : typeParameters)
				if (typeParameter.hasInferenceBlockingTypeParameters(parameters))
					return true;
		}
		
		return superType != null && superType.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + definition.hashCode();
		hash = 97 * hash + Arrays.deepHashCode(typeParameters);
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
				&& Arrays.deepEquals(this.typeParameters, other.typeParameters)
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
		for (int i = 0; i < typeParameters.length; i++) { 
			if (i > 0)
				result.append(", ");
			result.append(typeParameters[i].toString());
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
		for (ITypeID type : this.typeParameters)
			type.extractTypeParameters(typeParameters);
	}
	
	@Override
	public boolean isDestructible() {
		return definition.isDestructible();
	}

	public DefinitionTypeID getInnerType(GenericName name, GlobalTypeRegistry registry) {
		HighLevelDefinition type = definition.getInnerType(name.name);
		return registry.getForDefinition(type, name.arguments, this);
	}
}
