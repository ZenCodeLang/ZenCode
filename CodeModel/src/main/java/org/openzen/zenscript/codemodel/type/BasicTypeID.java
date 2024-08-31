package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.builtin.BasicTypeMembers;

import java.util.List;
import java.util.Optional;

public enum BasicTypeID implements TypeID, TypeSymbol {
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

	private final String name;
	private ResolvingType members;

	private Expression defaultValue = null;

	BasicTypeID(String name) {
		this.name = name;
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
	public boolean isValueType() {
		return true;
	}

	@Override
	public ResolvingType resolve() {
		if (members == null)
			members = BasicTypeMembers.get(this);

		return members;
	}

	@Override
	public boolean hasDefaultValue() {
		return true;
	}

	@Override
	public Expression getDefaultValue() {
		if (defaultValue == null) // lazy init due to circular dependency in the constant expressions
			defaultValue = generateDefaultValue();

		return defaultValue;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		// BasicTypeIDs don't have type parameters
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

	// #################################
	// ### TypeSymbol implementation ###
	// #################################

	@Override
	public ModuleSymbol getModule() {
		return ModuleSymbol.BUILTIN;
	}

	@Override
	public String describe() {
		return name;
	}

	@Override
	public boolean isInterface() {
		return false;
	}

	@Override
	public boolean isExpansion() {
		return false;
	}

	@Override
	public Modifiers getModifiers() {
		return Modifiers.PUBLIC;
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public boolean isEnum() {
		return false;
	}

	@Override
	public boolean isInvalid() {
		return this == INVALID;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ResolvingType resolve(TypeID[] typeArguments) {
		if(typeArguments.length > 0) {
			throw new IllegalArgumentException(this + " cannot have type arguments");
		}

		return this.resolve();
	}

	@Override
	public TypeParameter[] getTypeParameters() {
		return TypeParameter.NONE;
	}

	@Override
	public Optional<TypeSymbol> getOuter() {
		return Optional.empty();
	}

	@Override
	public Optional<TypeID> getSupertype(TypeID[] typeArguments) {
		return Optional.empty();
	}
}
