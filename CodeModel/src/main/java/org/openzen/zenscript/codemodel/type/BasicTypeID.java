/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public enum BasicTypeID implements ITypeID {
	VOID("void", "Void"),
	NULL("null", "Null"),
	ANY("any", "Any"),
	BOOL("bool", "Bool"),
	BYTE("byte", "Byte"),
	SBYTE("sbyte", "SByte"),
	SHORT("short", "Short"),
	USHORT("ushort", "UShort"),
	INT("int", "Int"),
	UINT("uint", "UInt"),
	LONG("long", "Long"),
	ULONG("ulong", "ULong"),
	FLOAT("float", "Float"),
	DOUBLE("double", "Double"),
	CHAR("char", "Char"),
	STRING("string", "String"),
	
	UNDETERMINED("undetermined", "Undetermined");
	
	public static final List<ITypeID> HINT_BOOL = Collections.singletonList(BOOL);
	
	private final String name;
	private final String camelCaseName;
	
	BasicTypeID(String name, String camelCaseName) {
		this.name = name;
		this.camelCaseName = camelCaseName;
	}
	
	@Override
	public ITypeID withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		return this;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitBasic(this);
	}
	
	@Override
	public BasicTypeID getUnmodified() {
		return this;
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return false;
	}

	@Override
	public String toCamelCaseName() {
		return camelCaseName;
	}
}
