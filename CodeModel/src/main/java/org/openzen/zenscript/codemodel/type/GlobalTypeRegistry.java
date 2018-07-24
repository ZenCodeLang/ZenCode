/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

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
	
	private final Map<ModifiedTypeID, ModifiedTypeID> modifiedTypes = new HashMap<>();
	
	public final ZSPackage stdlib;
	
	public GlobalTypeRegistry(ZSPackage stdlib) {
		this.stdlib = stdlib;
		
		arrayTypes.put(ArrayTypeID.INT, ArrayTypeID.INT);
		arrayTypes.put(ArrayTypeID.CHAR, ArrayTypeID.CHAR);
		
		rangeTypes.put(RangeTypeID.INT, RangeTypeID.INT);
	}
	
	public ArrayTypeID getArray(ITypeID baseType, int dimension) {
		ArrayTypeID id = new ArrayTypeID(this, baseType, dimension);
		if (arrayTypes.containsKey(id)) {
			return arrayTypes.get(id);
		} else {
			arrayTypes.put(id, id);
			return id;
		}
	}
	
	public AssocTypeID getAssociative(ITypeID keyType, ITypeID valueType) {
		AssocTypeID id = new AssocTypeID(this, keyType, valueType);
		if (assocTypes.containsKey(id)) {
			return assocTypes.get(id);
		} else {
			assocTypes.put(id, id);
			return id;
		}
	}
	
	public GenericMapTypeID getGenericMap(ITypeID valueType, TypeParameter key) {
		GenericMapTypeID id = new GenericMapTypeID(this, valueType, key);
		if (genericMapTypes.containsKey(id)) {
			return genericMapTypes.get(id);
		} else {
			genericMapTypes.put(id, id);
			return id;
		}
	}
	
	public IteratorTypeID getIterator(ITypeID[] loopTypes) {
		IteratorTypeID id = new IteratorTypeID(this, loopTypes);
		if (iteratorTypes.containsKey(id)) {
			return iteratorTypes.get(id);
		} else {
			iteratorTypes.put(id, id);
			return id;
		}
	}
	
	public FunctionTypeID getFunction(FunctionHeader header) {
		FunctionTypeID id = new FunctionTypeID(this, header);
		if (functionTypes.containsKey(id)) {
			return functionTypes.get(id);
		} else {
			functionTypes.put(id, id);
			return id;
		}
	}
	
	public RangeTypeID getRange(ITypeID from, ITypeID to) {
		RangeTypeID id = new RangeTypeID(this, from, to);
		if (rangeTypes.containsKey(id)) {
			return rangeTypes.get(id);
		} else {
			rangeTypes.put(id, id);
			return id;
		}
	}
	
	public GenericTypeID getGeneric(TypeParameter parameter) {
		GenericTypeID id = new GenericTypeID(parameter);
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
			for (int i = 0; i < definition.genericParameters.length; i++)
				typeArguments[i] = getGeneric(definition.genericParameters[i]);
		}
		DefinitionTypeID outer = null;
		if (definition.outerDefinition != null)
			outer = getForMyDefinition(definition.outerDefinition);
		
		return getForDefinition(definition, typeArguments, outer);
	}
	
	public DefinitionTypeID getForDefinition(HighLevelDefinition definition, ITypeID... genericArguments) {
		return this.getForDefinition(definition, genericArguments, null);
	}
	
	public DefinitionTypeID getForDefinition(HighLevelDefinition definition, ITypeID[] typeParameters, DefinitionTypeID outer) {
		DefinitionTypeID id = new DefinitionTypeID(this, definition, typeParameters, outer);
		
		if (definitionTypes.containsKey(id)) {
			return definitionTypes.get(id);
		} else {
			definitionTypes.put(id, id);
			return id;
		}
	}
	
	public ITypeID getOptional(ITypeID original) {
		return getModified(TypeMembers.MODIFIER_OPTIONAL, original);
	}
	
	public ITypeID getModified(int modifiers, ITypeID type) {
		if (modifiers == 0)
			return type;
		if (type instanceof ModifiedTypeID) {
			ModifiedTypeID modified = (ModifiedTypeID)type;
			return getModified(modified.modifiers | modifiers, modified.baseType);
		}
		
		ModifiedTypeID result = new ModifiedTypeID(this, modifiers, type);
		if (modifiedTypes.containsKey(result)) {
			return modifiedTypes.get(result);
		} else {
			modifiedTypes.put(result, result);
			return result;
		}
	}
}
