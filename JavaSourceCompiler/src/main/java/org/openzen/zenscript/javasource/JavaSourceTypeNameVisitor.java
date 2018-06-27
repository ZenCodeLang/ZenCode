/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ConstTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.ITypeVisitor;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceTypeNameVisitor implements ITypeVisitor<String> {
	public JavaSourceTypeNameVisitor() {}

	@Override
	public String visitBasic(BasicTypeID basic) {
		switch (basic) {
			case ANY: return "Any";
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
			case FLOAT: return "Float";
			case DOUBLE: return "Double";
			case STRING: return "String";
			case CHAR: return "Char";
			default: throw new IllegalArgumentException("Invalid type: " + basic);
		}
	}

	@Override
	public String visitArray(ArrayTypeID array) {
		if (array.dimension == 1) {
			return array.elementType.accept(this) + "Array";
		} else {
			return array.elementType.accept(this) + array.dimension + "DArray";
		}
	}

	@Override
	public String visitAssoc(AssocTypeID assoc) {
		return assoc.keyType.accept(this) + assoc.valueType.accept(this) + "Map";
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
			result.append(parameter.type.accept(this));
		result.append("To");
		result.append(function.header.returnType.accept(this));
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
		return range.from == range.to
				? range.from.accept(this) + "Range"
				: range.from.accept(this) + range.to.accept(this) + "Range";
	}

	@Override
	public String visitConst(ConstTypeID type) {
		return "Const" + type.accept(this);
	}

	@Override
	public String visitOptional(OptionalTypeID optional) {
		return "Optional" + optional.accept(this);
	}
}
