/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.TypeVisitorWithContext;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSyntheticTypeSignatureConverter implements TypeVisitorWithContext<StoredType, String, RuntimeException> {
	private static final String[] TYPE_PARAMETER_NAMES = {"T", "U", "V", "W", "X", "Y", "Z"}; // if we have more than this, make it Tx with x the number
	private final Map<TypeParameter, String> typeParameters = new HashMap<>();
	public final List<TypeParameter> typeParameterList = new ArrayList<>();
	
	private String process(StoredType type) {
		return type.type.accept(type, this);
	}
	
	@Override
	public String visitBasic(StoredType context, BasicTypeID basic) {
		switch (basic) {
			case VOID: return "Void";
			case BOOL: return "Bool";
			case BYTE: return "Byte";
			case SBYTE: return "SByte";
			case SHORT: return "Short";
			case USHORT: return "UShort";
			case INT: return "Int";
			case UINT: return "UInt";
			case LONG: return "Long";
			case ULONG: return "ULong";
			case USIZE: return "USize";
			case FLOAT: return "Float";
			case DOUBLE: return "Double";
			case CHAR: return "Char";
			default:
				throw new IllegalArgumentException("Invalid type: " + basic);
		}
	}
	
	@Override
	public String visitString(StoredType context, StringTypeID string) {
		return "String";
	}

	@Override
	public String visitArray(StoredType context, ArrayTypeID array) {
		StringBuilder result = new StringBuilder();
		result.append(process(array.elementType));
		if (array.dimension > 1)
			result.append(array.dimension).append("D");
		result.append("Array");
		return result.toString();
	}

	@Override
	public String visitAssoc(StoredType context, AssocTypeID assoc) {
		StringBuilder result = new StringBuilder();
		result.append(process(assoc.keyType));
		result.append(process(assoc.valueType));
		result.append("Map");
		return result.toString();
	}

	@Override
	public String visitGenericMap(StoredType context, GenericMapTypeID map) {
		return map.value.type.accept(map.value, this).concat("GenericMap");
	}

	@Override
	public String visitIterator(StoredType context, IteratorTypeID iterator) {
		StringBuilder result = new StringBuilder();
		for (StoredType type : iterator.iteratorTypes)
			result.append(process(type));
		result.append("Iterator");
		return result.toString();
	}

	@Override
	public String visitFunction(StoredType context, FunctionTypeID function) {
		StringBuilder result = new StringBuilder();
		for (FunctionParameter parameter : function.header.parameters)
			result.append(process(parameter.type));
		result.append("To");
		result.append(process(function.header.getReturnType()));
		if (function.header.thrownType != null) {
			result.append("Throwing");
			result.append(process(function.header.thrownType));
		}
		return result.toString();
	}

	@Override
	public String visitDefinition(StoredType context, DefinitionTypeID definition) {
		StringBuilder result = new StringBuilder();
		result.append(definition.definition.name);
		if (definition.typeArguments.length > 0) {
			result.append("With");
			for (int i = 0; i < definition.typeArguments.length; i++) {
				result.append(definition.typeArguments[i].type.accept(null, this));
			}
		}
		return result.toString();
	}

	@Override
	public String visitGeneric(StoredType context, GenericTypeID generic) {
		if (typeParameters.containsKey(generic.parameter))
			return typeParameters.get(generic.parameter);
		
		String name = typeParameters.size() < TYPE_PARAMETER_NAMES.length ? TYPE_PARAMETER_NAMES[typeParameters.size()] : "T" + typeParameters.size();
		typeParameters.put(generic.parameter, name);
		typeParameterList.add(generic.parameter);
		return name;
	}

	@Override
	public String visitRange(StoredType context, RangeTypeID range) {
		StringBuilder result = new StringBuilder();
		result.append(process(range.baseType));
		result.append("Range");
		return result.toString();
	}

	@Override
	public String visitModified(StoredType context, ModifiedTypeID type) {
		StringBuilder result = new StringBuilder();
		if (type.isConst())
			result.append("Const");
		if (type.isImmutable())
			result.append("Immutable");
		if (type.isOptional())
			result.append("Optional");
		
		result.append(type.baseType.accept(null, this));
		return result.toString();
	}
}
