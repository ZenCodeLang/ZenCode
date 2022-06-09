package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import static org.openzen.zenscript.codemodel.type.BasicTypeID.BYTE;

public enum BuiltinFieldSymbol implements FieldSymbol {
	BYTE_GET_MIN_VALUE(BYTE, "MIN_VALUE", BYTE),
	BYTE_GET_MAX_VALUE(BYTE, "MAX_VALUE", BYTE),

	/* ... */
	;

	private final TypeSymbol definingType;
	private final String name;
	private final TypeID type;

	BuiltinFieldSymbol(TypeSymbol definingType, String name, TypeID type) {
		this.definingType = definingType;
		this.name = name;
		this.type = type;
	}

	@Override
	public TypeSymbol getDefiningType() {
		return definingType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TypeID getType() {
		return type;
	}
}
