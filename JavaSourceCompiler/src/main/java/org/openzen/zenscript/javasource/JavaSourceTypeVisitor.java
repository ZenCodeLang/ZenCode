/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

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
public class JavaSourceTypeVisitor implements ITypeVisitor<String> {
	private final JavaSourceFile file;
	
	public JavaSourceTypeVisitor(JavaSourceFile file) {
		this.file = file;
	}

	@Override
	public String visitBasic(BasicTypeID basic) {
		switch (basic) {
			case VOID: return "void";
			case ANY: return "Any";
			case BOOL: return "boolean";
			case BYTE: return "byte";
			case SBYTE: return "byte";
			case SHORT: return "short";
			case USHORT: return "ushort";
			case INT: return "int";
			case UINT: return "int";
			case LONG: return "long";
			case ULONG: return "long";
			case FLOAT: return "float";
			case DOUBLE: return "double";
			case CHAR: return "char";
			case STRING: return "String";
			default:
				throw new IllegalArgumentException("Unknown basic type: " + basic);
		}
	}

	@Override
	public String visitArray(ArrayTypeID array) {
		StringBuilder result = new StringBuilder();
		result.append(array.elementType.accept(this));
		for (int i = 0; i < array.dimension; i++)
			result.append("[]");
		
		return result.toString();
	}

	@Override
	public String visitAssoc(AssocTypeID assoc) {
		String map = file.importType("java.util.Map");
		return map + "<" + assoc.keyType.accept(new JavaSourceObjectTypeVisitor(file)) + ", " + assoc.valueType.accept(new JavaSourceObjectTypeVisitor(file)) + ">";
	}

	@Override
	public String visitGenericMap(GenericMapTypeID map) {
		String javaMap = file.importType("java.util.Map");
		if (map.keys.length > 1)
			throw new UnsupportedOperationException("Not yet supported");
		
		return javaMap + "<Class<?>, " + map.value.accept(this) + ">";
	}

	@Override
	public String visitIterator(IteratorTypeID iterator) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String visitFunction(FunctionTypeID function) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String visitDefinition(DefinitionTypeID definition) {
		String javaType = file.importType(definition.definition);
		StringBuilder result = new StringBuilder(javaType);
		if (definition.typeParameters != null && definition.typeParameters.length > 0) {
			result.append("<");
			for (int i = 0; i < definition.typeParameters.length; i++) {
				if (i > 0)
					result.append(", ");
				result.append(definition.typeParameters[i].accept(this));
			}
			result.append(">");
		}
		return result.toString();
	}

	@Override
	public String visitGeneric(GenericTypeID generic) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String visitRange(RangeTypeID range) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String visitConst(ConstTypeID type) {
		return type.baseType.accept(this);
	}

	@Override
	public String visitOptional(OptionalTypeID optional) {
		return optional.baseType.accept(new JavaSourceObjectTypeVisitor(file));
	}
}
