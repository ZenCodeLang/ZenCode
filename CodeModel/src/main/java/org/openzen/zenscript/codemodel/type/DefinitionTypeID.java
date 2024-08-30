package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;

import java.util.*;

public class DefinitionTypeID implements TypeID {
	public static TypeID create(TypeSymbol definition, TypeID... typeArguments) {
		return definition.normalize(typeArguments);
	}

	public static TypeID createThis(TypeSymbol definition) {
		if (definition.getTypeParameters() == null)
			throw new AssertionError("Definition has null type parameters: " + definition.getName());

		return create(definition, Arrays.stream(definition.getTypeParameters()).map(GenericTypeID::new).toArray(TypeID[]::new));
	}

	public final TypeSymbol definition;
	public final TypeID[] typeArguments;
	public final DefinitionTypeID original;
	public final DefinitionTypeID outer;

	public DefinitionTypeID(TypeSymbol definition, TypeID[] typeArguments, DefinitionTypeID original) {
		this(definition, typeArguments, original, null);
	}

	// For inner classes of generic outer classes
	public DefinitionTypeID(TypeSymbol definition, TypeID[] typeArguments, DefinitionTypeID original, DefinitionTypeID outer) {
		if (typeArguments == null)
			throw new NullPointerException("typeParameters cannot be null");
		if (typeArguments.length != definition.getTypeParameters().length)
			throw new IllegalArgumentException("Wrong number of type parameters! " + definition.getName() + " expected: " + definition.getTypeParameters().length + " got: " + typeArguments.length);
		if (definition.getOuter().isPresent() && !definition.isStatic() && outer == null)
			throw new IllegalArgumentException("Inner definition requires outer instance");
		if ((!definition.getOuter().isPresent() || definition.isStatic()) && outer != null)
			throw new IllegalArgumentException("Static inner definition must not have outer instance");

		this.definition = definition;
		this.typeArguments = typeArguments;
		this.original = original;
		this.outer = outer;
	}

	/**
	 * Resolves this type to its type members. Excludes applicable expansions!
	 *
	 * @return base type members
	 */
	public ResolvingType resolve() {
		return definition.resolve(this, typeArguments);
	}

	public boolean hasTypeParameters() {
		return typeArguments.length > 0;
	}

	public Map<TypeParameter, TypeID> getTypeParameterMapping() {
		Map<TypeParameter, TypeID> mapping = new HashMap<>();
		DefinitionTypeID current = this;
		do {
			if (current.typeArguments != null) {
				if (current.definition.getTypeParameters() != null) {
					for (int i = 0; i < current.typeArguments.length; i++)
						mapping.put(current.definition.getTypeParameters()[i], current.typeArguments[i]);
				}
			}

			current = current.outer;
		} while (current != null && !current.definition.isStatic());
		return mapping;
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		if (!hasTypeParameters() && outer == null)
			return this;
		if (mapper == null || mapper.getMapping().isEmpty())
			return this;

		TypeID[] instancedArguments = TypeID.NONE;
		if (hasTypeParameters()) {
			instancedArguments = new TypeID[typeArguments.length];
			for (int i = 0; i < typeArguments.length; i++)
				instancedArguments[i] = typeArguments[i].instance(mapper);
		}

		DefinitionTypeID instancedOuter = outer == null ? null : (DefinitionTypeID) outer.instance(mapper);
		return new DefinitionTypeID(definition, instancedArguments, original, instancedOuter);
	}

	@Override
	public TypeID getSuperType() {
		return definition.getSupertype(typeArguments).orElse(null);
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
	public Optional<DefinitionTypeID> asDefinition() {
		return Optional.of(this);
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
		return definition.isEnum();
	}

	@Override
	public boolean isVariant() {
		return definition instanceof VariantDefinition;
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
			return definition.getName();

		StringBuilder result = new StringBuilder();
		if (outer != null)
			result.append(outer).append('.');

		result.append(definition.getName());
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
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		for (TypeID type : this.typeArguments)
			type.extractTypeParameters(typeParameters);
	}

	/*@Override
	public Expression castImplicitFrom(CodePosition position, Expression value) {
		return new SubtypeCastExpression(position, value, this);
	}*/
}
