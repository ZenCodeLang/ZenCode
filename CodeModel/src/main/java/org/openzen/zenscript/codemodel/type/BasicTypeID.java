/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public enum BasicTypeID implements ITypeID {
	VOID("void"),
	NULL("null"),
	BOOL("bool"),
	BYTE("byte"),
	SBYTE("sbyte"),
	SHORT("short"),
	USHORT("ushort"),
	INT("int"),
	UINT("uint"),
	LONG("long"),
	ULONG("ulong"),
	FLOAT("float"),
	DOUBLE("double"),
	CHAR("char"),
	STRING("string"),
	
	UNDETERMINED("undetermined");
	
	public static final List<ITypeID> HINT_BOOL = Collections.singletonList(BOOL);
	
	public final String name;
	
	BasicTypeID(String name) {
		this.name = name;
	}
	
	@Override
	public BasicTypeID getNormalized() {
		return this;
	}
	
	@Override
	public ITypeID instance(GenericMapper mapper) {
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
	public boolean isObjectType() {
		return this == STRING;
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return false;
	}

	@Override
	public boolean hasDefaultValue() {
		return true;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		
	}
}
