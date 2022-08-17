package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import static org.openzen.zenscript.codemodel.type.BasicTypeID.BYTE;

public enum BuiltinFieldSymbol implements FieldSymbol {
	BYTE_GET_MIN_VALUE(BYTE, Modifiers.PUBLIC_STATIC, "MIN_VALUE", BYTE),
	BYTE_GET_MAX_VALUE(BYTE, Modifiers.PUBLIC_STATIC, "MAX_VALUE", BYTE),

	/* ... */
	;

	private static final Modifiers MODIFIERS = Modifiers.PUBLIC.withStatic();

	private final TypeSymbol definingType;
	private final Modifiers modifiers;
	private final String name;
	private final TypeID type;

	BuiltinFieldSymbol(TypeSymbol definingType, Modifiers modifiers, String name, TypeID type) {
		this.definingType = definingType;
		this.modifiers = modifiers;
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

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}
}
