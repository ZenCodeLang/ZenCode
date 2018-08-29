/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.ITypeVisitor;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaTypeNameVisitor implements ITypeVisitor<String> {
	public JavaTypeNameVisitor() {}

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
		result.append(function.header.getReturnType().accept(this));
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
	public String visitModified(ModifiedTypeID type) {
		StringBuilder result = new StringBuilder();
		if (type.isConst())
			result.append("Const");
		if (type.isOptional())
			result.append("Optional");
		if (type.isImmutable())
			result.append("Immutable");
		result.append(type.accept(this));
		return result.toString();
	}
}
