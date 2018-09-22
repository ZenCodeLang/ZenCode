/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.HashMap;
import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class GlobalTypeRegistry {
	private final Map<StringTypeID, StringTypeID> stringTypes = new HashMap<>();
	private final Map<ArrayTypeID, ArrayTypeID> arrayTypes = new HashMap<>();
	private final Map<AssocTypeID, AssocTypeID> assocTypes = new HashMap<>();
	private final Map<GenericMapTypeID, GenericMapTypeID> genericMapTypes = new HashMap<>();
	private final Map<IteratorTypeID, IteratorTypeID> iteratorTypes = new HashMap<>();
	private final Map<FunctionTypeID, FunctionTypeID> functionTypes = new HashMap<>();
	private final Map<RangeTypeID, RangeTypeID> rangeTypes = new HashMap<>();
	private final Map<DefinitionTypeID, DefinitionTypeID> definitionTypes = new HashMap<>();
	private final Map<GenericTypeID, GenericTypeID> genericTypes = new HashMap<>();
	
	private final Map<ModifiedTypeID, ModifiedTypeID> modifiedTypes = new HashMap<>();
	
	public final ZSPackage stdlib;
	
	public GlobalTypeRegistry(ZSPackage stdlib) {
		this.stdlib = stdlib;
		
		stringTypes.put(StringTypeID.NOSTORAGE, StringTypeID.NOSTORAGE);
		stringTypes.put(StringTypeID.ANY, StringTypeID.ANY);
		stringTypes.put(StringTypeID.BORROW, StringTypeID.BORROW);
		stringTypes.put(StringTypeID.SHARED, StringTypeID.SHARED);
		stringTypes.put(StringTypeID.STATIC, StringTypeID.STATIC);
		stringTypes.put(StringTypeID.UNIQUE, StringTypeID.UNIQUE);
		
		arrayTypes.put(ArrayTypeID.INT_UNIQUE, ArrayTypeID.INT_UNIQUE);
		arrayTypes.put(ArrayTypeID.CHAR_UNIQUE, ArrayTypeID.CHAR_UNIQUE);
		
		rangeTypes.put(RangeTypeID.INT, RangeTypeID.INT);
		rangeTypes.put(RangeTypeID.USIZE, RangeTypeID.USIZE);
	}
	
	public StringTypeID getString(StorageTag storage) {
		StringTypeID id = new StringTypeID(storage);
		return internalize(stringTypes, id);
	}
	
	public ArrayTypeID getArray(ITypeID baseType, int dimension, StorageTag storage) {
		ArrayTypeID id = new ArrayTypeID(this, baseType, dimension, storage);
		return internalize(arrayTypes, id);
	}
	
	public AssocTypeID getAssociative(ITypeID keyType, ITypeID valueType, StorageTag storage) {
		AssocTypeID id = new AssocTypeID(this, keyType, valueType, storage);
		return internalize(assocTypes, id);
	}
	
	public GenericMapTypeID getGenericMap(ITypeID valueType, TypeParameter key, StorageTag storage) {
		GenericMapTypeID id = new GenericMapTypeID(this, valueType, key, storage);
		return internalize(genericMapTypes, id);
	}
	
	public IteratorTypeID getIterator(ITypeID[] loopTypes, StorageTag storage) {
		IteratorTypeID id = new IteratorTypeID(this, loopTypes, storage);
		return internalize(iteratorTypes, id);
	}
	
	public FunctionTypeID getFunction(FunctionHeader header, StorageTag storage) {
		FunctionTypeID id = new FunctionTypeID(this, header, storage);
		if (functionTypes.containsKey(id)) {
			return functionTypes.get(id);
		} else {
			functionTypes.put(id, id);
			return id;
		}
	}
	
	public RangeTypeID getRange(ITypeID type) {
		RangeTypeID id = new RangeTypeID(this, type);
		if (rangeTypes.containsKey(id)) {
			return rangeTypes.get(id);
		} else {
			rangeTypes.put(id, id);
			return id;
		}
	}
	
	public GenericTypeID getGeneric(TypeParameter parameter, StorageTag storage) {
		GenericTypeID id = new GenericTypeID(this, parameter, storage);
		if (genericTypes.containsKey(id)) {
			return genericTypes.get(id);
		} else {
			genericTypes.put(id, id);
			return id;
		}
	}
	
	public DefinitionTypeID getForMyDefinition(HighLevelDefinition definition) {
		ITypeID[] typeArguments = ITypeID.NONE;
		if (definition.getNumberOfGenericParameters() > 0) {
			typeArguments = new ITypeID[definition.getNumberOfGenericParameters()];
			for (int i = 0; i < definition.typeParameters.length; i++)
				typeArguments[i] = getGeneric(definition.typeParameters[i], null);
		}
		DefinitionTypeID outer = null;
		if (definition.outerDefinition != null)
			outer = getForMyDefinition(definition.outerDefinition);
		
		return getForDefinition(definition, typeArguments, outer, BorrowStorageTag.THIS);
	}
	
	public DefinitionTypeID getForDefinition(HighLevelDefinition definition, StorageTag storage, ITypeID... genericArguments) {
		return this.getForDefinition(definition, genericArguments, null, storage);
	}
	
	public DefinitionTypeID getForDefinition(HighLevelDefinition definition, ITypeID[] typeParameters, DefinitionTypeID outer, StorageTag storage) {
		DefinitionTypeID id = new DefinitionTypeID(this, definition, typeParameters, definition.isStatic() ? null : outer, storage);
		
		if (definitionTypes.containsKey(id)) {
			return definitionTypes.get(id);
		} else {
			definitionTypes.put(id, id);
			return id;
		}
	}
	
	public ITypeID getOptional(ITypeID original) {
		return getModified(ModifiedTypeID.MODIFIER_OPTIONAL, original);
	}
	
	public ITypeID getModified(int modifiers, ITypeID type) {
		if (modifiers == 0)
			return type;
		if (type instanceof ModifiedTypeID) {
			ModifiedTypeID modified = (ModifiedTypeID)type;
			return getModified(modified.modifiers | modifiers, modified.baseType);
		}
		
		ModifiedTypeID result = new ModifiedTypeID(this, modifiers, type);
		return internalize(modifiedTypes, result);
	}
	
	private <T> T internalize(Map<T, T> identityMap, T id) {
		if (identityMap.containsKey(id)) {
			return identityMap.get(id);
		} else {
			identityMap.put(id, id);
			return id;
		}
	}
}
