package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.util.Collections;
import java.util.List;

public enum BasicTypeID implements TypeID {
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
	USIZE("usize"),
	FLOAT("float"),
	DOUBLE("double"),
	CHAR("char"),
	STRING("string"),

	UNDETERMINED("undetermined"),
	INVALID("invalid");

	public static final List<TypeID> HINT_BOOL = Collections.singletonList(BOOL);

	public final String name;

	private Expression defaultValue = null;

	BasicTypeID(String name) {
		this.name = name;
	}

	@Override
	public BasicTypeID getNormalized() {
		return this;
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		return this;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitBasic(this);
	}

	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitBasic(context, this);
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isValueType() {
		return true;
	}

	@Override
	public boolean hasDefaultValue() {
		return true;
	}

	@Override
	public Expression getDefaultValue() {
		if (defaultValue == null) // lazy init due to circular initialization in the constant expressions
			defaultValue = generateDefaultValue();

		return defaultValue;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {

	}

	private Expression generateDefaultValue() {
		switch (this) {
			case NULL:
				return new NullExpression(CodePosition.UNKNOWN);
			case BOOL:
				return new ConstantBoolExpression(CodePosition.UNKNOWN, false);
			case BYTE:
				return new ConstantByteExpression(CodePosition.UNKNOWN, 0);
			case SBYTE:
				return new ConstantSByteExpression(CodePosition.UNKNOWN, (byte) 0);
			case SHORT:
				return new ConstantShortExpression(CodePosition.UNKNOWN, (short) 0);
			case USHORT:
				return new ConstantUShortExpression(CodePosition.UNKNOWN, 0);
			case INT:
				return new ConstantIntExpression(CodePosition.UNKNOWN, 0);
			case UINT:
				return new ConstantUIntExpression(CodePosition.UNKNOWN, 0);
			case LONG:
				return new ConstantLongExpression(CodePosition.UNKNOWN, 0);
			case ULONG:
				return new ConstantULongExpression(CodePosition.UNKNOWN, 0);
			case USIZE:
				return new ConstantUSizeExpression(CodePosition.UNKNOWN, 0);
			case FLOAT:
				return new ConstantFloatExpression(CodePosition.UNKNOWN, 0);
			case DOUBLE:
				return new ConstantDoubleExpression(CodePosition.UNKNOWN, 0);
			default:
				return null;
		}
	}
}
