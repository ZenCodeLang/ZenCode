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
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.TypeVisitorWithContext;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaTypeNameVisitor implements TypeVisitorWithContext<StoredType, String, RuntimeException> {
	public static final JavaTypeNameVisitor INSTANCE = new JavaTypeNameVisitor();
	
	private JavaTypeNameVisitor() {}

	public String process(StoredType type) {
		return type.type.accept(type, this);
	}
	
	public String process(TypeID type) {
		return type.accept(null, this);
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
			case FLOAT: return "Float";
			case DOUBLE: return "Double";
			case CHAR: return "Char";
			default: throw new IllegalArgumentException("Invalid type: " + basic);
		}
	}
	
	@Override
	public String visitString(StoredType context, StringTypeID string) {
		return "String";
	}

	@Override
	public String visitArray(StoredType context, ArrayTypeID array) {
		if (array.dimension == 1) {
			return process(array.elementType) + "Array";
		} else {
			return process(array.elementType) + array.dimension + "DArray";
		}
	}

	@Override
	public String visitAssoc(StoredType context, AssocTypeID assoc) {
		return process(assoc.keyType) + process(assoc.valueType) + "Map";
	}

	@Override
	public String visitGenericMap(StoredType context, GenericMapTypeID map) {
		return "GenericMap"; // TODO: make this better?
	}

	@Override
	public String visitIterator(StoredType context, IteratorTypeID iterator) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String visitFunction(StoredType context, FunctionTypeID function) {
		StringBuilder result = new StringBuilder();
		for (FunctionParameter parameter : function.header.parameters)
			result.append(process(parameter.type));
		result.append("To");
		result.append(process(function.header.getReturnType()));
		result.append("Function");
		return result.toString();
	}

	@Override
	public String visitDefinition(StoredType context, DefinitionTypeID definition) {
		return definition.definition.name;
	}

	@Override
	public String visitGeneric(StoredType context, GenericTypeID generic) {
		return generic.parameter.name;
	}

	@Override
	public String visitRange(StoredType context, RangeTypeID range) {
		return range.baseType.type.accept(range.baseType, this) + "Range";
	}

	@Override
	public String visitModified(StoredType context, OptionalTypeID type) {
		StringBuilder result = new StringBuilder();
		if (type.isOptional())
			result.append("Optional");
		result.append(type.baseType.accept(context, this));
		return result.toString();
	}
}
