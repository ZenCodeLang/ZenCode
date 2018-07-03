/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.type.BasicTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceObjectTypeVisitor extends JavaSourceTypeVisitor {
	public JavaSourceObjectTypeVisitor(JavaSourceImporter importer, JavaSourceSyntheticTypeGenerator typeGenerator) {
		super(importer, typeGenerator);
	}
	
	@Override
	public String visitBasic(BasicTypeID basic) {
		switch (basic) {
			case VOID: return "Void";
			case ANY: return "Any";
			case BOOL: return "Boolean";
			case BYTE: return "Byte";
			case SBYTE: return "Byte";
			case SHORT: return "Short";
			case USHORT: return "Short";
			case INT: return "Integer";
			case UINT: return "Integer";
			case LONG: return "Long";
			case ULONG: return "Long";
			case FLOAT: return "Float";
			case DOUBLE: return "Double";
			case CHAR: return "Character";
			case STRING: return "String";
			default:
				throw new IllegalArgumentException("Unknown basic type: " + basic);
		}
	}
}
