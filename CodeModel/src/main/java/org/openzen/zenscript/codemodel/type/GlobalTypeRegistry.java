/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import static org.openzen.zenscript.codemodel.type.member.TypeMembers.MODIFIER_CONST;
import static org.openzen.zenscript.codemodel.type.member.TypeMembers.MODIFIER_OPTIONAL;
import static org.openzen.zenscript.codemodel.type.member.TypeMembers.MODIFIER_SHARED;
import static org.openzen.zenscript.codemodel.type.member.TypeMembers.MODIFIER_WEAK;

/**
 *
 * @author Hoofdgebruiker
 */
public class GlobalTypeRegistry {
	private final Map<ArrayTypeID, ArrayTypeID> arrayTypes = new HashMap<>();
	private final Map<AssocTypeID, AssocTypeID> assocTypes = new HashMap<>();
	private final Map<IteratorTypeID, IteratorTypeID> iteratorTypes = new HashMap<>();
	private final Map<FunctionTypeID, FunctionTypeID> functionTypes = new HashMap<>();
	private final Map<RangeTypeID, RangeTypeID> rangeTypes = new HashMap<>();
	private final Map<DefinitionTypeID, DefinitionTypeID> definitionTypes = new HashMap<>();
	private final Map<GenericTypeID, GenericTypeID> genericTypes = new HashMap<>();
	
	private final Map<ITypeID, ConstTypeID> constTypes = new HashMap<>();
	private final Map<ITypeID, OptionalTypeID> optionalTypes = new HashMap<>();
	
	public ArrayTypeID getArray(ITypeID baseType, int dimension) {
		ArrayTypeID id = new ArrayTypeID(baseType, dimension);
		if (arrayTypes.containsKey(id)) {
			return arrayTypes.get(id);
		} else {
			arrayTypes.put(id, id);
			return id;
		}
	}
	
	public AssocTypeID getAssociative(ITypeID keyType, ITypeID valueType) {
		AssocTypeID id = new AssocTypeID(keyType, valueType);
		if (assocTypes.containsKey(id)) {
			return assocTypes.get(id);
		} else {
			assocTypes.put(id, id);
			return id;
		}
	}
	
	public IteratorTypeID getIterator(ITypeID[] loopTypes) {
		IteratorTypeID id = new IteratorTypeID(loopTypes);
		if (iteratorTypes.containsKey(id)) {
			return iteratorTypes.get(id);
		} else {
			iteratorTypes.put(id, id);
			return id;
		}
	}
	
	public FunctionTypeID getFunction(FunctionHeader header) {
		FunctionTypeID id = new FunctionTypeID(header);
		if (functionTypes.containsKey(id)) {
			return functionTypes.get(id);
		} else {
			functionTypes.put(id, id);
			return id;
		}
	}
	
	public RangeTypeID getRange(ITypeID from, ITypeID to) {
		RangeTypeID id = new RangeTypeID(from, to);
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
	
	public DefinitionTypeID getForDefinition(HighLevelDefinition definition, List<ITypeID> genericArguments) {
		return this.getForDefinition(definition, genericArguments, Collections.emptyMap());
	}
	
	public DefinitionTypeID getForDefinition(HighLevelDefinition definition, List<ITypeID> typeParameters, Map<TypeParameter, ITypeID> outerInstance) {
		DefinitionTypeID id;
		if (definition.genericParameters.length > 0 && typeParameters.isEmpty() && outerInstance.isEmpty()) {
			// make it a static one
			id = new StaticDefinitionTypeID(definition);
		} else {
			id = new DefinitionTypeID(definition, typeParameters.toArray(new ITypeID[typeParameters.size()]), outerInstance);
		}
		
		if (definitionTypes.containsKey(id)) {
			return definitionTypes.get(id);
		} else {
			definitionTypes.put(id, id);
			id.init(this);
			return id;
		}
	}
	
	private ConstTypeID getConst(ITypeID original) {
		if (constTypes.containsKey(original)) {
			return constTypes.get(original);
		} else {
			ConstTypeID result = new ConstTypeID(original);
			constTypes.put(original, result);
			return result;
		}
	}
	
	public OptionalTypeID getOptional(ITypeID original) {
		if (optionalTypes.containsKey(original)) {
			return optionalTypes.get(original);
		} else {
			OptionalTypeID result = new OptionalTypeID(original);
			optionalTypes.put(original, result);
			return result;
		}
	}
	
	public ITypeID getModified(int modifiers, ITypeID type) {
		if (modifiers == 0)
			return type;
		
		if ((modifiers & MODIFIER_OPTIONAL) > 0)
			return getModified(modifiers & ~MODIFIER_OPTIONAL, getOptional(type));
		if ((modifiers & MODIFIER_CONST) > 0)
			return getModified(modifiers & ~MODIFIER_CONST, getConst(type));
		if ((modifiers & MODIFIER_SHARED) > 0)
			throw new UnsupportedOperationException();
		if ((modifiers & MODIFIER_WEAK) > 0)
			throw new UnsupportedOperationException();
		
		throw new UnsupportedOperationException();
	}
}
