package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GlobalTypeRegistry {
	public final ZSPackage stdlib;
	// Allows for internalizing any TypeID not defined here.
	private final Map<Class<?>, Map<?, ? extends TypeID>> identityMaps = new HashMap<>();

	private final Map<ArrayTypeID, ArrayTypeID> arrayTypes = new HashMap<>();
	private final Map<AssocTypeID, AssocTypeID> assocTypes = new HashMap<>();
	private final Map<GenericMapTypeID, GenericMapTypeID> genericMapTypes = new HashMap<>();
	private final Map<IteratorTypeID, IteratorTypeID> iteratorTypes = new HashMap<>();
	private final Map<FunctionTypeID, FunctionTypeID> functionTypes = new HashMap<>();
	private final Map<RangeTypeID, RangeTypeID> rangeTypes = new HashMap<>();
	private final Map<DefinitionTypeID, DefinitionTypeID> definitionTypes = new HashMap<>();
	private final Map<GenericTypeID, GenericTypeID> genericTypes = new HashMap<>();
	private final Map<OptionalTypeID, OptionalTypeID> optionalTypes = new HashMap<>();

	public GlobalTypeRegistry(ZSPackage stdlib) {
		this.stdlib = stdlib;

		arrayTypes.put(ArrayTypeID.INT, ArrayTypeID.INT);
		arrayTypes.put(ArrayTypeID.CHAR, ArrayTypeID.CHAR);

		rangeTypes.put(RangeTypeID.INT, RangeTypeID.INT);
		rangeTypes.put(RangeTypeID.USIZE, RangeTypeID.USIZE);
		identityMaps.put(ArrayTypeID.class, arrayTypes);
		identityMaps.put(AssocTypeID.class, assocTypes);
		identityMaps.put(GenericMapTypeID.class, genericMapTypes);
		identityMaps.put(IteratorTypeID.class, iteratorTypes);
		identityMaps.put(FunctionTypeID.class, functionTypes);
		identityMaps.put(RangeTypeID.class, rangeTypes);
		identityMaps.put(DefinitionTypeID.class, definitionTypes);
		identityMaps.put(GenericTypeID.class, genericTypes);
		identityMaps.put(OptionalTypeID.class, optionalTypes);
	}

	public ArrayTypeID getArray(TypeID baseType, int dimension) {
		ArrayTypeID id = new ArrayTypeID(this, baseType, dimension);
		return internalize(arrayTypes, id);
	}

	public AssocTypeID getAssociative(TypeID keyType, TypeID valueType) {
		AssocTypeID id = new AssocTypeID(this, keyType, valueType);
		return internalize(assocTypes, id);
	}

	public GenericMapTypeID getGenericMap(TypeID valueType, TypeParameter key) {
		GenericMapTypeID id = new GenericMapTypeID(this, valueType, key);
		return internalize(genericMapTypes, id);
	}

	public IteratorTypeID getIterator(TypeID[] loopTypes) {
		IteratorTypeID id = new IteratorTypeID(this, loopTypes);
		return internalize(iteratorTypes, id);
	}

	public FunctionTypeID getFunction(FunctionHeader header) {
		FunctionTypeID id = new FunctionTypeID(this, header);
		return internalize(functionTypes, id);
	}

	public RangeTypeID getRange(TypeID type) {
		RangeTypeID id = new RangeTypeID(this, type);
		return internalize(rangeTypes, id);
	}

	public GenericTypeID getGeneric(TypeParameter parameter) {
		GenericTypeID id = new GenericTypeID(parameter);
		return internalize(genericTypes, id);
	}

	public DefinitionTypeID getForMyDefinition(HighLevelDefinition definition) {
		TypeID[] typeArguments = TypeID.NONE;
		if (definition.getNumberOfGenericParameters() > 0) {
			typeArguments = new TypeID[definition.getNumberOfGenericParameters()];
			for (int i = 0; i < definition.typeParameters.length; i++)
				typeArguments[i] = getGeneric(definition.typeParameters[i]);
		}
		DefinitionTypeID outer = null;
		if (definition.outerDefinition != null)
			outer = getForMyDefinition(definition.outerDefinition);

		return getForDefinition(definition, typeArguments, outer);
	}

	public DefinitionTypeID getForDefinition(HighLevelDefinition definition, TypeID... typeArguments) {
		return this.getForDefinition(definition, typeArguments, null);
	}

	public DefinitionTypeID getForDefinition(HighLevelDefinition definition, TypeID[] typeArguments, DefinitionTypeID outer) {
		DefinitionTypeID id = new DefinitionTypeID(this, definition, typeArguments, definition.isStatic() ? null : outer);
		return internalize(definitionTypes, id);
	}

	public TypeID getOptional(TypeID original) {
		return internalize(optionalTypes, new OptionalTypeID(this, original));
	}

	private <T> T internalize(Map<T, T> identityMap, T id) {
		if (identityMap.containsKey(id)) {
			return identityMap.get(id);
		} else {
			identityMap.put(id, id);
			return id;
		}
	}

	public <T extends TypeID> T internalize(Class<T> clazz, T id) {
		Map<T, T> identityMap = (Map<T, T>) identityMaps.computeIfAbsent(clazz, aClass -> new HashMap<>());
		if (identityMap.containsKey(id)) {
			return identityMap.get(id);
		} else {
			identityMap.put(id, id);
			return id;
		}
	}

	public <ID, TYPE extends TypeID> TYPE internalize(Class<TYPE> typeClass, ID id, Function<ID, TYPE> generator) {

		Map<ID, TYPE> identityMap = (Map<ID, TYPE>) identityMaps.computeIfAbsent(typeClass, aClass -> new HashMap<>());
		if (identityMap.containsKey(id)) {
			return identityMap.get(id);
		} else {
			TYPE value = generator.apply(id);
			identityMap.put(id, value);
			return value;
		}
	}

	public Collection<DefinitionTypeID> getDefinitions() {
		return definitionTypes.keySet();
	}
}
