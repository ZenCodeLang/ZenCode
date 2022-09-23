package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.builtin.ArrayTypeSymbol;

import java.util.List;
import java.util.Optional;

public class ArrayTypeID implements TypeID {
	public static final ArrayTypeID SBYTE = new ArrayTypeID(BasicTypeID.SBYTE, 1);
	public static final ArrayTypeID BYTE = new ArrayTypeID(BasicTypeID.BYTE, 1);
	public static final ArrayTypeID SHORT = new ArrayTypeID(BasicTypeID.SHORT, 1);
	public static final ArrayTypeID USHORT = new ArrayTypeID(BasicTypeID.USHORT, 1);
	public static final ArrayTypeID INT = new ArrayTypeID(BasicTypeID.INT, 1);
	public static final ArrayTypeID UINT = new ArrayTypeID(BasicTypeID.UINT, 1);
	public static final ArrayTypeID LONG = new ArrayTypeID(BasicTypeID.LONG, 1);
	public static final ArrayTypeID ULONG = new ArrayTypeID(BasicTypeID.ULONG, 1);
	public static final ArrayTypeID CHAR = new ArrayTypeID(BasicTypeID.CHAR, 1);

	public final TypeID elementType;
	public final int dimension;
	private final TypeSymbol type;

	public ArrayTypeID(TypeID elementType) {
		this(elementType, 1);
	}

	public ArrayTypeID(TypeID elementType, int dimension) {
		this.elementType = elementType;
		this.dimension = dimension;
		type = ArrayTypeSymbol.get(dimension);
	}

	@Override
	public Expression getDefaultValue() {
		return new ArrayExpression(CodePosition.UNKNOWN, Expression.NONE, this);
	}

	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitArray(this);
	}

	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitArray(context, this);
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isValueType() {
		return false;
	}

	@Override
	public Optional<ArrayTypeID> asArray() {
		return Optional.of(this);
	}

	@Override
	public ResolvedType resolve() {
		return type.resolve(new TypeID[] { elementType });
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		return new ArrayTypeID(elementType.instance(mapper), dimension);
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		elementType.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + elementType.hashCode();
		hash = 79 * hash + dimension;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final ArrayTypeID other = (ArrayTypeID) obj;
		return this.dimension == other.dimension && this.elementType.equals(other.elementType);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(elementType.toStringSuffixed());
		result.append('[');
		for (int i = 1; i < dimension; i++) {
			result.append(',');
		}
		result.append(']');
		return result.toString();
	}
}
