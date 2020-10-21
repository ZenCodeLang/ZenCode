/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public enum ParsedTypeBasic implements IParsedType {
	VOID(BasicTypeID.VOID),
	BOOL(BasicTypeID.BOOL),
	BYTE(BasicTypeID.BYTE),
	SBYTE(BasicTypeID.SBYTE),
	SHORT(BasicTypeID.SHORT),
	USHORT(BasicTypeID.USHORT),
	INT(BasicTypeID.INT),
	UINT(BasicTypeID.UINT),
	LONG(BasicTypeID.LONG),
	ULONG(BasicTypeID.ULONG),
	USIZE(BasicTypeID.USIZE),
	FLOAT(BasicTypeID.FLOAT),
	DOUBLE(BasicTypeID.DOUBLE),
	CHAR(BasicTypeID.CHAR),
	STRING(BasicTypeID.STRING),
	
	UNDETERMINED(BasicTypeID.UNDETERMINED);
	
	private final BasicTypeID type;

	private ParsedTypeBasic(BasicTypeID type) {
		this.type = type;
	}
	
	@Override
	public TypeID compile(TypeResolutionContext context) {
		return type;
	}
}
