package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;

public enum BuiltinFieldSymbol implements FieldSymbol {
	BYTE_MIN_VALUE(BYTE, Modifiers.CONST, "MIN_VALUE", BYTE),
	BYTE_MAX_VALUE(BYTE, Modifiers.CONST, "MAX_VALUE", BYTE),

	SBYTE_MIN_VALUE(SBYTE, Modifiers.CONST, "MIN_VALUE", SBYTE),
	SBYTE_MAX_VALUE(SBYTE, Modifiers.CONST, "MAX_VALUE", SBYTE),

	SHORT_MIN_VALUE(SHORT, Modifiers.CONST, "MIN_VALUE", SHORT),
	SHORT_MAX_VALUE(SHORT, Modifiers.CONST, "MAX_VALUE", SHORT),

	USHORT_MIN_VALUE(USHORT, Modifiers.CONST, "MIN_VALUE", USHORT),
	USHORT_MAX_VALUE(USHORT, Modifiers.CONST, "MAX_VALUE", USHORT),

	INT_MIN_VALUE(INT, Modifiers.CONST, "MIN_VALUE", INT),
	INT_MAX_VALUE(INT, Modifiers.CONST, "MAX_VALUE", INT),

	UINT_MIN_VALUE(UINT, Modifiers.CONST, "MIN_VALUE", UINT),
	UINT_MAX_VALUE(UINT, Modifiers.CONST, "MAX_VALUE", UINT),

	LONG_MIN_VALUE(LONG, Modifiers.CONST, "MIN_VALUE", LONG),
	LONG_MAX_VALUE(LONG, Modifiers.CONST, "MAX_VALUE", LONG),

	ULONG_MIN_VALUE(ULONG, Modifiers.CONST, "MIN_VALUE", ULONG),
	ULONG_MAX_VALUE(ULONG, Modifiers.CONST, "MAX_VALUE", ULONG),

	USIZE_MIN_VALUE(USIZE, Modifiers.CONST, "MIN_VALUE", USIZE),
	USIZE_MAX_VALUE(USIZE, Modifiers.CONST, "MAX_VALUE", USIZE),
	USIZE_BITS(USIZE, Modifiers.CONST, "BITS", UINT),

	FLOAT_MIN_VALUE(FLOAT, Modifiers.CONST, "MIN_VALUE", FLOAT),
	FLOAT_MAX_VALUE(FLOAT, Modifiers.CONST, "MAX_VALUE", FLOAT),

	DOUBLE_MIN_VALUE(DOUBLE, Modifiers.CONST, "MIN_VALUE", DOUBLE),
	DOUBLE_MAX_VALUE(DOUBLE, Modifiers.CONST, "MAX_VALUE", DOUBLE),

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
