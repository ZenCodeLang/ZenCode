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
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionTypeID implements ITypeID {
	public static DefinitionTypeID forType(GlobalTypeRegistry registry, HighLevelDefinition definition, StorageTag storage) {
		if (definition.typeParameters != null && definition.typeParameters.length > 0)
			throw new IllegalArgumentException("Definition has type arguments!");
		
		return new DefinitionTypeID(registry, definition, ITypeID.NONE, storage);
	}
	
	public final HighLevelDefinition definition;
	public final ITypeID[] typeArguments;
	public final DefinitionTypeID outer;
	public final StorageTag storage;
	private ITypeID normalized;
	private ITypeID withoutStorage;
	
	public ITypeID superType;
	
	public DefinitionTypeID(GlobalTypeRegistry typeRegistry, HighLevelDefinition definition, ITypeID[] typeParameters, StorageTag storage) {
		this(typeRegistry, definition, typeParameters, null, storage);
	}
	
	// For inner classes of generic outer classes
	public DefinitionTypeID(GlobalTypeRegistry typeRegistry, HighLevelDefinition definition, ITypeID[] typeParameters, DefinitionTypeID outer, StorageTag storage) {
		if (typeParameters == null)
			throw new NullPointerException("typeParameters cannot be null");
		if (typeParameters.length != definition.getNumberOfGenericParameters())
			throw new IllegalArgumentException("Wrong number of type parameters!");
		if (definition.isInnerDefinition() && !definition.isStatic() && outer == null)
			throw new IllegalArgumentException("Inner definition requires outer instance");
		if ((!definition.isInnerDefinition() || definition.isStatic()) && outer != null)
			throw new IllegalArgumentException("Static inner definition must not have outer instance");
		
		this.definition = definition;
		this.typeArguments = typeParameters;
		this.outer = outer;
		this.storage = storage;
		
		normalized = isDenormalized() ? normalize(typeRegistry) : this;
		if (normalized instanceof DefinitionTypeID && ((DefinitionTypeID)normalized).isDenormalized())
			throw new AssertionError();
		
		withoutStorage = storage == null ? this : typeRegistry.getForDefinition(definition, typeParameters, outer, null);
	}
	
	private boolean isDenormalized() {
		if (definition instanceof AliasDefinition)
			return true;
		
		for (ITypeID typeParameter : typeArguments)
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
			for (int i = 0; i < definition.typeParameters.length; i++)
				typeMapping.put(definition.typeParameters[i], typeArguments[i].getNormalized());
			GenericMapper mapper = new GenericMapper(typeRegistry, typeMapping);
			ITypeID result = alias.type.instance(mapper).getNormalized();
			return result;
		}
		
		ITypeID[] normalizedTypeParameters = new ITypeID[typeArguments.length];
		for (int i = 0; i < normalizedTypeParameters.length; i++)
			normalizedTypeParameters[i] = typeArguments[i].getNormalized();
		
		return typeRegistry.getForDefinition(definition, normalizedTypeParameters, outer == null ? null : (DefinitionTypeID)outer.getNormalized(), storage);
	}
	
	public boolean hasTypeParameters() {
		return typeArguments.length > 0;
	}
	
	public Map<TypeParameter, ITypeID> getTypeParameterMapping() {
		Map<TypeParameter, ITypeID> mapping = new HashMap<>();
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
	
	public DefinitionTypeID(HighLevelDefinition definition, StorageTag storage) {
		this.definition = definition;
		this.typeArguments = ITypeID.NONE;
		this.superType = definition.getSuperType();
		this.outer = null;
		this.storage = storage;
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
			instancedArguments = new ITypeID[typeArguments.length];
			for (int i = 0; i < typeArguments.length; i++)
				instancedArguments[i] = typeArguments[i].instance(mapper);
		}
		
		DefinitionTypeID instancedOuter = outer == null ? null : outer.instance(mapper);
		return mapper.registry.getForDefinition(definition, instancedArguments, instancedOuter, storage);
	}

	@Override
	public DefinitionTypeID withStorage(GlobalTypeRegistry registry, StorageTag storage) {
		return registry.getForDefinition(definition, typeArguments, outer, storage);
	}
	
	@Override
	public ITypeID getSuperType(GlobalTypeRegistry registry) {
		return definition.getSuperType() == null ? null : definition.getSuperType().instance(new GenericMapper(registry, getTypeParameterMapping()));
	}
	
	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitDefinition(this);
	}
	
	@Override
	public <C, R> R accept(C context, TypeVisitorWithContext<C, R> visitor) {
		return visitor.visitDefinition(context, this);
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
			for (ITypeID typeParameter : typeArguments)
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
		hash = 97 * hash + Objects.hashCode(storage);
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
				&& Objects.equals(outer, this.outer)
				&& Objects.equals(this.storage, other.storage);
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
		for (ITypeID type : this.typeArguments)
			type.extractTypeParameters(typeParameters);
	}
	
	@Override
	public boolean isDestructible() {
		return definition.isDestructible();
	}

	public DefinitionTypeID getInnerType(GenericName name, GlobalTypeRegistry registry, StorageTag storage) {
		HighLevelDefinition type = definition.getInnerType(name.name);
		return registry.getForDefinition(type, name.arguments, this, storage);
	}

	@Override
	public StorageTag getStorage() {
		return storage;
	}

	@Override
	public ITypeID withoutStorage() {
		return withoutStorage;
	}
}
