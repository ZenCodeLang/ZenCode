package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.util.List;

public class ArrayTypeID implements TypeID {
	public static final ArrayTypeID INT = new ArrayTypeID(BasicTypeID.INT, 1);
	public static final ArrayTypeID CHAR = new ArrayTypeID(BasicTypeID.CHAR, 1);

	public final TypeID elementType;
	public final int dimension;
	private final ArrayTypeID normalized;

	private ArrayTypeID(TypeID elementType, int dimension) {
		this.elementType = elementType;
		this.dimension = dimension;
		this.normalized = this;
	}

	public ArrayTypeID(GlobalTypeRegistry registry, TypeID elementType, int dimension) {
		this.elementType = elementType;
		this.dimension = dimension;
		this.normalized = elementType.getNormalized() == elementType ? this : registry.getArray(elementType.getNormalized(), dimension);
	}

	public TypeID removeOneDimension() {
		return dimension > 1 ? new ArrayTypeID(elementType, dimension - 1) : elementType;
	}

	@Override
	public Expression getDefaultValue() {
		return new ArrayExpression(CodePosition.UNKNOWN, Expression.NONE, this);
	}

	@Override
	public ArrayTypeID getNormalized() {
		return normalized;
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
	public TypeID instance(GenericMapper mapper) {
		return mapper.registry.getArray(elementType.instance(mapper), dimension);
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return elementType.hasInferenceBlockingTypeParameters(parameters);
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ArrayTypeID other = (ArrayTypeID) obj;
		return this.dimension == other.dimension
				&& this.elementType.equals(other.elementType);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(elementType.toString());
		result.append('[');
		for (int i = 1; i < dimension; i++) {
			result.append(',');
		}
		result.append(']');
		return result.toString();
	}
}
