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
import org.openzen.zenscript.codemodel.type.*;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSyntheticTypeSignatureConverter implements TypeVisitor<String> {
	private static final String[] TYPE_PARAMETER_NAMES = {"T", "U", "V", "W", "X", "Y", "Z"}; // if we have more than this, make it Tx with x the number
	private final Map<TypeParameter, String> typeParameters = new HashMap<>();
	public final List<TypeParameter> typeParameterList = new ArrayList<>();
	
	private String process(TypeID type) {
		return type.accept(this);
	}
	
	@Override
	public String visitBasic(BasicTypeID basic) {
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
			case STRING: return "String";
			default:
				throw new IllegalArgumentException("Invalid type: " + basic);
		}
	}
	
	@Override
	public String visitArray(ArrayTypeID array) {
		StringBuilder result = new StringBuilder();
		result.append(process(array.elementType));
		if (array.dimension > 1)
			result.append(array.dimension).append("D");
		result.append("Array");
		return result.toString();
	}

	@Override
	public String visitAssoc(AssocTypeID assoc) {
		return process(assoc.keyType) + process(assoc.valueType) + "Map";
	}

	@Override
	public String visitGenericMap(GenericMapTypeID map) {
		return map.value.accept(this).concat("GenericMap");
	}

	@Override
	public String visitIterator(IteratorTypeID iterator) {
		StringBuilder result = new StringBuilder();
		for (TypeID type : iterator.iteratorTypes)
			result.append(process(type));
		result.append("Iterator");
		return result.toString();
	}

	@Override
	public String visitFunction(FunctionTypeID function) {
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
	public String visitDefinition(DefinitionTypeID definition) {
		StringBuilder result = new StringBuilder();
		result.append(definition.definition.name);
		if (definition.typeArguments.length > 0) {
			result.append("With");
			for (int i = 0; i < definition.typeArguments.length; i++) {
				result.append(definition.typeArguments[i].accept(this));
			}
		}
		return result.toString();
	}

	@Override
	public String visitGeneric(GenericTypeID generic) {
		if (typeParameters.containsKey(generic.parameter))
			return typeParameters.get(generic.parameter);
		
		String name = typeParameters.size() < TYPE_PARAMETER_NAMES.length ? TYPE_PARAMETER_NAMES[typeParameters.size()] : "T" + typeParameters.size();
		typeParameters.put(generic.parameter, name);
		typeParameterList.add(generic.parameter);
		return name;
	}

	@Override
	public String visitRange(RangeTypeID range) {
		return process(range.baseType) + "Range";
	}

	@Override
	public String visitOptional(OptionalTypeID type) {
		StringBuilder result = new StringBuilder();
		if (type.isOptional())
			result.append("Optional");
		
		result.append(type.baseType.accept( this));
		return result.toString();
	}
}
