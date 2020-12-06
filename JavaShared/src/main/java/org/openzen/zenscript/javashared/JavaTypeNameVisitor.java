/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.type.*;

/**
 * @author Hoofdgebruiker
 */
public class JavaTypeNameVisitor implements TypeVisitor<String> {
	public static final JavaTypeNameVisitor INSTANCE = new JavaTypeNameVisitor();

	private JavaTypeNameVisitor() {
	}

	public String process(TypeID type) {
		return type.accept(this);
	}

	@Override
	public String visitBasic(BasicTypeID basic) {
		switch (basic) {
			case VOID:
				return "Void";
			case BOOL:
				return "Bool";
			case BYTE:
				return "Byte";
			case SBYTE:
				return "SByte";
			case SHORT:
				return "Short";
			case USHORT:
				return "UShort";
			case INT:
				return "Int";
			case UINT:
				return "UInt";
			case LONG:
				return "Long";
			case ULONG:
				return "ULong";
			case FLOAT:
				return "Float";
			case DOUBLE:
				return "Double";
			case CHAR:
				return "Char";
			case STRING:
				return "String";
			default:
				throw new IllegalArgumentException("Invalid type: " + basic);
		}
	}

	@Override
	public String visitArray(ArrayTypeID array) {
		if (array.dimension == 1) {
			return process(array.elementType) + "Array";
		} else {
			return process(array.elementType) + array.dimension + "DArray";
		}
	}

	@Override
	public String visitAssoc(AssocTypeID assoc) {
		return process(assoc.keyType) + process(assoc.valueType) + "Map";
	}

	@Override
	public String visitGenericMap(GenericMapTypeID map) {
		return "GenericMap"; // TODO: make this better?
	}

	@Override
	public String visitIterator(IteratorTypeID iterator) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String visitFunction(FunctionTypeID function) {
		StringBuilder result = new StringBuilder();
		for (FunctionParameter parameter : function.header.parameters)
			result.append(process(parameter.type));
		result.append("To");
		result.append(process(function.header.getReturnType()));
		result.append("Function");
		return result.toString();
	}

	@Override
	public String visitDefinition(DefinitionTypeID definition) {
		return definition.definition.name;
	}

	@Override
	public String visitGeneric(GenericTypeID generic) {
		return generic.parameter.name;
	}

	@Override
	public String visitRange(RangeTypeID range) {
		return range.baseType.accept(this) + "Range";
	}

	@Override
	public String visitOptional(OptionalTypeID type) {
		StringBuilder result = new StringBuilder();
		if (type.isOptional())
			result.append("Optional");
		result.append(type.baseType.accept(this));
		return result.toString();
	}
}
