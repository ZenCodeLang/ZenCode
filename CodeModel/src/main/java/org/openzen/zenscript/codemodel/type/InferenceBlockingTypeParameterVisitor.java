package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Visitor to determine if a type has inference blocking type parameters.
 * <p>
 * This works by adding the known types to a map, and quering the map to see if
 * a type has been seen before.
 * <p>
 * Types are first added to the map with a value of false, to indicate that it is
 * currently being visited.
 * <p>
 * This is done to resolve circular types (see MyEnum extends Enum<MyEnum>).
 * <p>
 * After extra processing is done (such as {@link ArrayTypeID} checking its element's type to determine if it blocks),
 * the actual result of if the type blocks is inserted into the map.
 */
public class InferenceBlockingTypeParameterVisitor implements TypeVisitor<Boolean> {

	private final TypeParameter[] parameters;
	private final Map<TypeID, Boolean> visitedTypes;

	public InferenceBlockingTypeParameterVisitor(TypeParameter[] parameters) {
		this.parameters = parameters;
		this.visitedTypes = new HashMap<>();
	}

	@Override
	public Boolean visitBasic(BasicTypeID basic) {
		Optional<Boolean> knownResult = getKnownResult(basic);
		if (knownResult.isPresent()) {
			return knownResult.get();
		}
		visitedTypes.put(basic, false);
		return false;
	}

	@Override
	public Boolean visitArray(ArrayTypeID array) {
		Optional<Boolean> knownResult = getKnownResult(array);
		if (knownResult.isPresent()) {
			return knownResult.get();
		}

		visitedTypes.put(array, false);

		TypeID elementType = array.elementType;
		Optional<Boolean> knownElementType = getKnownResult(elementType);
		if (knownElementType.isPresent()) {
			visitedTypes.put(array, knownElementType.get());
			return knownElementType.get();
		}
		visitedTypes.putIfAbsent(elementType, false);
		Boolean blocking = elementType.accept(this);
		visitedTypes.put(elementType, blocking);
		visitedTypes.put(array, blocking);
		return blocking;
	}

	@Override
	public Boolean visitAssoc(AssocTypeID assoc) {
		Optional<Boolean> knownResult = getKnownResult(assoc);
		if (knownResult.isPresent()) {
			return knownResult.get();
		}
		visitedTypes.put(assoc, false);
		TypeID keyType = assoc.keyType;
		Optional<Boolean> knownKeyType = getKnownResult(keyType);
		if (knownKeyType.isPresent()) {
			visitedTypes.put(assoc, knownKeyType.get());
			return knownKeyType.get();
		}
		visitedTypes.putIfAbsent(keyType, false);
		Boolean keyBlocking = keyType.accept(this);
		visitedTypes.put(keyType, keyBlocking);
		visitedTypes.put(assoc, keyBlocking);
		if (keyBlocking) {
			return true;
		}
		TypeID valueType = assoc.valueType;
		Optional<Boolean> knownValueType = getKnownResult(valueType);
		if (knownValueType.isPresent()) {
			visitedTypes.put(assoc, knownValueType.get());
			return knownValueType.get();
		}
		visitedTypes.putIfAbsent(valueType, false);
		Boolean valueBlocking = valueType.accept(this);
		visitedTypes.put(valueType, valueBlocking);
		visitedTypes.put(assoc, valueBlocking);
		return valueBlocking;
	}

	@Override
	public Boolean visitGenericMap(GenericMapTypeID map) {
		Optional<Boolean> knownResult = getKnownResult(map);
		if (knownResult.isPresent()) {
			return knownResult.get();
		}
		visitedTypes.put(map, false);
		TypeID value = map.value;
		Optional<Boolean> knownValue = getKnownResult(value);
		if (knownValue.isPresent()) {
			visitedTypes.put(map, knownValue.get());
			return knownValue.get();
		}
		visitedTypes.putIfAbsent(value, false);
		Boolean blocking = value.accept(this);
		visitedTypes.put(value, blocking);
		visitedTypes.put(map, blocking);
		return blocking;
	}

	@Override
	public Boolean visitIterator(IteratorTypeID iterator) {
		Optional<Boolean> knownResult = getKnownResult(iterator);
		if (knownResult.isPresent()) {
			return knownResult.get();
		}
		visitedTypes.put(iterator, false);
		for (TypeID type : iterator.iteratorTypes) {
			Optional<Boolean> knownType = getKnownResult(type);
			if (knownType.isPresent() && knownType.get()) {
				visitedTypes.put(iterator, true);
				return true;
			}
			visitedTypes.put(type, false);
			Boolean blocking = type.accept(this);
			visitedTypes.put(type, blocking);
			if (blocking) {
				visitedTypes.put(iterator, true);
				return true;
			}
		}
		return false;
	}

	@Override
	public Boolean visitFunction(FunctionTypeID function) {
		Optional<Boolean> knownResult = getKnownResult(function);
		if (knownResult.isPresent()) {
			return knownResult.get();
		}
		visitedTypes.put(function, false);
		boolean blocking = function.header.hasInferenceBlockingTypeParameters(parameters);
		visitedTypes.put(function, blocking);
		return blocking;
	}

	@Override
	public Boolean visitDefinition(DefinitionTypeID definition) {
		Optional<Boolean> knownResult = getKnownResult(definition);
		if (knownResult.isPresent()) {
			return knownResult.get();
		}
		visitedTypes.put(definition, false);
		if (definition.hasTypeParameters()) {
			for (TypeID typeArgument : definition.typeArguments) {
				Optional<Boolean> knownType = getKnownResult(typeArgument);
				if (knownType.isPresent() && knownType.get()) {
					visitedTypes.put(definition, true);
					return true;
				}
				visitedTypes.put(typeArgument, false);
				boolean blocking = typeArgument.accept(this);
				visitedTypes.put(typeArgument, blocking);
				if (blocking) {
					visitedTypes.put(definition, true);
					return true;
				}
			}
		}

		return definition.definition.getSupertype(definition.typeArguments)
				.map(superType -> {
					Optional<Boolean> knownSuperType = getKnownResult(superType);
					if (knownSuperType.isPresent() && knownSuperType.get()) {
						visitedTypes.put(definition, true);
						return true;
					}
					visitedTypes.put(superType, false);
					Boolean blocking = superType.accept(this);
					visitedTypes.put(superType, blocking);
					if (blocking) {
						visitedTypes.put(definition, true);
						return true;
					}

					return false;
				}).orElse(false);
	}

	@Override
	public Boolean visitGeneric(GenericTypeID generic) {
		Optional<Boolean> knownResult = getKnownResult(generic);
		if (knownResult.isPresent()) {
			return knownResult.get();
		}
		for (TypeParameter parameter : parameters) {
			if (parameter == generic.parameter) {
				visitedTypes.put(generic, true);
				return true;
			}
		}
		visitedTypes.put(generic, false);
		return false;
	}

	@Override
	public Boolean visitRange(RangeTypeID range) {
		Optional<Boolean> knownResult = getKnownResult(range);
		if (knownResult.isPresent()) {
			return knownResult.get();
		}
		visitedTypes.put(range, false);
		TypeID baseType = range.baseType;
		Optional<Boolean> knownElementType = getKnownResult(baseType);
		if (knownElementType.isPresent()) {
			visitedTypes.put(range, knownElementType.get());
			return knownElementType.get();
		}
		visitedTypes.putIfAbsent(baseType, false);
		Boolean blocking = baseType.accept(this);
		visitedTypes.put(baseType, blocking);
		visitedTypes.put(range, blocking);
		return blocking;
	}

	@Override
	public Boolean visitOptional(OptionalTypeID type) {
		Optional<Boolean> knownResult = getKnownResult(type);
		if (knownResult.isPresent()) {
			return knownResult.get();
		}
		visitedTypes.put(type, false);
		TypeID baseType = type.baseType;
		Optional<Boolean> knownElementType = getKnownResult(baseType);
		if (knownElementType.isPresent()) {
			visitedTypes.put(type, knownElementType.get());
			return knownElementType.get();
		}
		visitedTypes.putIfAbsent(baseType, false);
		Boolean blocking = baseType.accept(this);
		visitedTypes.put(baseType, blocking);
		visitedTypes.put(type, blocking);
		return blocking;
	}

	@Override
	public Boolean visitInvalid(InvalidTypeID type) {
		Optional<Boolean> knownResult = getKnownResult(type);
		if (knownResult.isPresent()) {
			return knownResult.get();
		}
		visitedTypes.put(type, false);
		return false;
	}

	private Optional<Boolean> getKnownResult(TypeID type) {
		if (visitedTypes.containsKey(type)) {
			return Optional.of(visitedTypes.get(type));
		}
		return Optional.empty();
	}
}
