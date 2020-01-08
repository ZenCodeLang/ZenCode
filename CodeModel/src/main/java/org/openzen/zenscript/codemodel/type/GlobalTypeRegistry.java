/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class GlobalTypeRegistry {
	private final Map<ArrayTypeID, ArrayTypeID> arrayTypes = new HashMap<>();
	private final Map<AssocTypeID, AssocTypeID> assocTypes = new HashMap<>();
	private final Map<GenericMapTypeID, GenericMapTypeID> genericMapTypes = new HashMap<>();
	private final Map<IteratorTypeID, IteratorTypeID> iteratorTypes = new HashMap<>();
	private final Map<FunctionTypeID, FunctionTypeID> functionTypes = new HashMap<>();
	private final Map<RangeTypeID, RangeTypeID> rangeTypes = new HashMap<>();
	private final Map<DefinitionTypeID, DefinitionTypeID> definitionTypes = new HashMap<>();
	private final Map<GenericTypeID, GenericTypeID> genericTypes = new HashMap<>();
	
	private final Map<OptionalTypeID, OptionalTypeID> optionalTypes = new HashMap<>();
	
	public final ZSPackage stdlib;
	
	public GlobalTypeRegistry(ZSPackage stdlib) {
		this.stdlib = stdlib;
		
		arrayTypes.put(ArrayTypeID.INT, ArrayTypeID.INT);
		arrayTypes.put(ArrayTypeID.CHAR, ArrayTypeID.CHAR);
		
		rangeTypes.put(RangeTypeID.INT, RangeTypeID.INT);
		rangeTypes.put(RangeTypeID.USIZE, RangeTypeID.USIZE);
	}
	
	public ArrayTypeID getArray(StoredType baseType, int dimension) {
		ArrayTypeID id = new ArrayTypeID(this, baseType, dimension);
		return internalize(arrayTypes, id);
	}
	
	public AssocTypeID getAssociative(StoredType keyType, StoredType valueType) {
		AssocTypeID id = new AssocTypeID(this, keyType, valueType);
		return internalize(assocTypes, id);
	}
	
	public GenericMapTypeID getGenericMap(StoredType valueType, TypeParameter key) {
		GenericMapTypeID id = new GenericMapTypeID(this, valueType, key);
		return internalize(genericMapTypes, id);
	}
	
	public IteratorTypeID getIterator(StoredType[] loopTypes) {
		IteratorTypeID id = new IteratorTypeID(this, loopTypes);
		return internalize(iteratorTypes, id);
	}
	
	public FunctionTypeID getFunction(FunctionHeader header) {
		FunctionTypeID id = new FunctionTypeID(this, header);
		return internalize(functionTypes, id);
	}
	
	public RangeTypeID getRange(StoredType type) {
		RangeTypeID id = new RangeTypeID(this, type);
		return internalize(rangeTypes, id);
	}
	
	public GenericTypeID getGeneric(TypeParameter parameter) {
		GenericTypeID id = new GenericTypeID(parameter);
		return internalize(genericTypes, id);
	}
	
	public DefinitionTypeID getForMyDefinition(HighLevelDefinition definition) {
		StoredType[] typeArguments = StoredType.NONE;
		if (definition.getNumberOfGenericParameters() > 0) {
			typeArguments = new StoredType[definition.getNumberOfGenericParameters()];
			for (int i = 0; i < definition.typeParameters.length; i++)
				typeArguments[i] = new StoredType(getGeneric(definition.typeParameters[i]), definition.typeParameters[i].storage);
		}
		DefinitionTypeID outer = null;
		if (definition.outerDefinition != null)
			outer = getForMyDefinition(definition.outerDefinition);
		
		return getForDefinition(definition, typeArguments, outer);
	}
	
	public DefinitionTypeID getForDefinition(HighLevelDefinition definition, StoredType... typeArguments) {
		return this.getForDefinition(definition, typeArguments, null);
	}
	
	public DefinitionTypeID getForDefinition(HighLevelDefinition definition, StoredType[] typeArguments, DefinitionTypeID outer) {
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

	public Collection<DefinitionTypeID> getDefinitions() {
		return definitionTypes.keySet();
	}
}
