package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.util.*;

public class DefinitionTypeID implements TypeID {
	public final HighLevelDefinition definition;
	public final TypeID[] typeArguments;
	public final DefinitionTypeID outer;
	private TypeID normalized;

	public DefinitionTypeID(GlobalTypeRegistry typeRegistry, HighLevelDefinition definition, TypeID[] typeArguments) {
		this(typeRegistry, definition, typeArguments, null);
	}

	// For inner classes of generic outer classes
	public DefinitionTypeID(GlobalTypeRegistry typeRegistry, HighLevelDefinition definition, TypeID[] typeArguments, DefinitionTypeID outer) {
		if (typeArguments == null)
			throw new NullPointerException("typeParameters cannot be null");
		if (typeArguments.length != definition.getNumberOfGenericParameters())
			throw new IllegalArgumentException("Wrong number of type parameters! " + definition.name + " expected: " + definition.getNumberOfGenericParameters() + " got: " + typeArguments.length);
		if (definition.isInnerDefinition() && !definition.isStatic() && outer == null)
			throw new IllegalArgumentException("Inner definition requires outer instance");
		if ((!definition.isInnerDefinition() || definition.isStatic()) && outer != null)
			throw new IllegalArgumentException("Static inner definition must not have outer instance");

		this.definition = definition;
		this.typeArguments = typeArguments;
		this.outer = outer;

		normalized = isDenormalized() ? normalize(typeRegistry) : this;
		if (normalized instanceof DefinitionTypeID && ((DefinitionTypeID) normalized).isDenormalized())
			throw new AssertionError();
	}

	public DefinitionTypeID(HighLevelDefinition definition) {
		this.definition = definition;
		this.typeArguments = TypeID.NONE;
		this.outer = null;
	}

	private boolean isDenormalized() {
		if (definition instanceof AliasDefinition)
			return true;

		for (TypeID typeArgument : typeArguments)
			if (!typeArgument.getNormalized().equals(typeArgument))
				return true;

		return outer != null && !outer.getNormalized().equals(outer);
	}

	private TypeID normalize(GlobalTypeRegistry typeRegistry) {
		if (definition instanceof AliasDefinition) {
			AliasDefinition alias = (AliasDefinition) definition;
			if (alias.type == null)
				throw new IllegalStateException("Alias type not yet initialized!");

			Map<TypeParameter, TypeID> typeMapping = new HashMap<>();
			for (int i = 0; i < definition.typeParameters.length; i++)
				typeMapping.put(definition.typeParameters[i], typeArguments[i].getNormalized());
			GenericMapper mapper = new GenericMapper(definition.position, typeRegistry, typeMapping);
			return alias.type.instance(mapper).getNormalized();
		}

		TypeID[] normalizedTypeParameters = new TypeID[typeArguments.length];
		for (int i = 0; i < normalizedTypeParameters.length; i++)
			normalizedTypeParameters[i] = typeArguments[i].getNormalized();

		return typeRegistry.getForDefinition(definition, normalizedTypeParameters, outer == null ? null : (DefinitionTypeID) outer.getNormalized());
	}

	public boolean hasTypeParameters() {
		return typeArguments.length > 0;
	}

	public Map<TypeParameter, TypeID> getTypeParameterMapping() {
		Map<TypeParameter, TypeID> mapping = new HashMap<>();
		DefinitionTypeID current = this;
		do {
			if (current.typeArguments != null) {
				if (current.definition.typeParameters != null) {
					for (int i = 0; i < current.typeArguments.length; i++)
						mapping.put(current.definition.typeParameters[i], current.typeArguments[i]);
				}//else {
				//    System.out.println("Type parameters but no generic parameters");
				//}
			}

			current = current.outer;
		} while (current != null && !current.definition.isStatic());
		return mapping;
	}

	@Override
	public TypeID getNormalized() {
		return normalized;
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		if (!hasTypeParameters() && outer == null)
			return this;
		if (mapper == null || mapper.getMapping().isEmpty())
			return this;
		if (mapper.registry == null)
			throw new NullPointerException();

		TypeID[] instancedArguments = TypeID.NONE;
		if (hasTypeParameters()) {
			instancedArguments = new TypeID[typeArguments.length];
			for (int i = 0; i < typeArguments.length; i++)
				instancedArguments[i] = typeArguments[i].instance(mapper);
		}

		DefinitionTypeID instancedOuter = outer == null ? null : (DefinitionTypeID) outer.instance(mapper);
		return mapper.registry.getForDefinition(definition, instancedArguments, instancedOuter);
	}

	@Override
	public TypeID getSuperType(GlobalTypeRegistry registry) {
		return definition.getSuperType() == null ? null : definition.getSuperType().instance(new GenericMapper(definition.position, registry, getTypeParameterMapping()));
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
	public boolean isDefinition(HighLevelDefinition definition) {

		return this.definition.equals(definition);
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		if (hasTypeParameters()) {
			for (TypeID typeArgument : typeArguments)
				if (typeArgument.hasInferenceBlockingTypeParameters(parameters))
					return true;
		}

		TypeID superType = definition.getSuperType();
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
