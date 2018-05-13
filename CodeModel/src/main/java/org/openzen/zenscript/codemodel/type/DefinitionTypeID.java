/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionTypeID implements ITypeID {
	public static DefinitionTypeID forType(HighLevelDefinition definition) {
		if (definition.genericParameters != null)
			throw new IllegalArgumentException("Definition has type arguments!");
		
		return new DefinitionTypeID(definition, null);
	}
	
	private static final OuterTypeEntry[] NO_OUTER_ENTRIES = new OuterTypeEntry[0];
	
	public final HighLevelDefinition definition;
	public final ITypeID[] typeParameters;
	private final OuterTypeEntry[] outerTypeEntries;
	
	public ITypeID superType;
	public Map<TypeParameter, ITypeID> outerTypeParameters; // for nonstatic inner classes of generic types, contains the type parameters for the outer class(es)
	
	public DefinitionTypeID(HighLevelDefinition definition, ITypeID[] typeParameters) {
		this(definition, typeParameters, Collections.emptyMap());
	}
	
	// For inner classes of generic outer classes
	public DefinitionTypeID(HighLevelDefinition definition, ITypeID[] typeParameters, Map<TypeParameter, ITypeID> outerTypeParameters) {
		this.definition = definition;
		this.typeParameters = typeParameters;
		this.outerTypeParameters = outerTypeParameters;
		
		if (outerTypeParameters.isEmpty()) {
			this.outerTypeEntries = NO_OUTER_ENTRIES;
		} else {
			this.outerTypeEntries = new OuterTypeEntry[outerTypeParameters.size()];
			int index = 0;
			for (Map.Entry<TypeParameter, ITypeID> entry : outerTypeParameters.entrySet())
				outerTypeEntries[index++] = new OuterTypeEntry(entry.getKey(), entry.getValue());
			Arrays.sort(outerTypeEntries, (a, b) -> a.parameter.name.compareTo(b.parameter.name));
		}
		
		//if ((typeParameters == null ? 0 : typeParameters.length) != definition.getNumberOfGenericParameters())
		//	throw new RuntimeException("Invalid number of type parameters");
	}
	
	public boolean hasTypeParameters() {
		return typeParameters != null && typeParameters.length > 0;
	}
	
	public void init(GlobalTypeRegistry registry) {
		ITypeID superType = definition.superType;
		if (superType != null && hasTypeParameters()) {
			Map<TypeParameter, ITypeID> genericSuperArguments = new HashMap<>();
			for (int i = 0; i < typeParameters.length; i++)
				genericSuperArguments.put(definition.genericParameters[i], typeParameters[i]);
			
			superType = definition.superType.withGenericArguments(registry, genericSuperArguments);
		}
		this.superType = superType;
	}
	
	// To be used exclusively by StaticDefinitionTypeID
	protected DefinitionTypeID(HighLevelDefinition definition) {
		this.definition = definition;
		this.typeParameters = null;
		this.superType = definition.superType;
		this.outerTypeParameters = Collections.emptyMap();
		this.outerTypeEntries = NO_OUTER_ENTRIES;
	}
	
	@Override
	public ITypeID withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		if (!hasTypeParameters() && outerTypeParameters.isEmpty())
			return this;
		
		ITypeID[] instancedArguments = null;
		if (typeParameters != null) {
			instancedArguments = new ITypeID[typeParameters.length];
			for (int i = 0; i < typeParameters.length; i++) {
				// TODO: why was this line written like this?
				//instancedArguments[i] = arguments.containsKey(definition.genericParameters[i]) ? arguments.get(definition.genericParameters[i]) : typeParameters[i].withGenericArguments(registry, arguments);
				instancedArguments[i] = typeParameters[i].withGenericArguments(registry, arguments);
			}
		}
		
		Map<TypeParameter, ITypeID> instancedOuter;
		if (outerTypeParameters.isEmpty()) {
			instancedOuter = Collections.emptyMap();
		} else {
			instancedOuter = new HashMap<>();
			for (Map.Entry<TypeParameter, ITypeID> entry : outerTypeParameters.entrySet())
				instancedOuter.put(entry.getKey(), entry.getValue().withGenericArguments(registry, arguments));
		}
		return registry.getForDefinition(definition, instancedArguments, instancedOuter);
	}
	
	@Override
	public ITypeID getSuperType() {
		return superType;
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
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		if (typeParameters != null) {
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
		hash = 97 * hash + Arrays.deepHashCode(outerTypeEntries);
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
				&& Arrays.deepEquals(this.outerTypeEntries, other.outerTypeEntries);
	}
	
	@Override
	public String toString() {
		if (typeParameters == null) {
			return definition.name;
		} else {
			StringBuilder result = new StringBuilder();
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
	}

	@Override
	public boolean hasDefaultValue() {
		return definition.hasEmptyConstructor();
	}
	
	private class OuterTypeEntry {
		private final TypeParameter parameter;
		private final ITypeID type;
		
		public OuterTypeEntry(TypeParameter parameter, ITypeID type) {
			this.parameter = parameter;
			this.type = type;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 11 * hash + Objects.hashCode(this.parameter);
			hash = 11 * hash + Objects.hashCode(this.type);
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
			final OuterTypeEntry other = (OuterTypeEntry) obj;
			if (!Objects.equals(this.parameter, other.parameter)) {
				return false;
			}
			if (!Objects.equals(this.type, other.type)) {
				return false;
			}
			return true;
		}
	}
}
