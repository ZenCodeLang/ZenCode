package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.util.List;
import java.util.Objects;

/**
 * Represents a wildcard type with an upper bound. (eg. ? extends Number)
 * <p>
 * This is used in the context of type arguments, where the type argument is not known, but it is known that it is a
 * subtype of the given bound.
 */
public class WildcardOutTypeID implements TypeID {
	public final TypeID upperBound;

	public WildcardOutTypeID(TypeID upperBound) {
		this.upperBound = upperBound;
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		TypeID mappedUpperBound = upperBound.instance(mapper);
		return mappedUpperBound == upperBound ? this : new WildcardOutTypeID(mappedUpperBound);
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		upperBound.extractTypeParameters(typeParameters);
	}

	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitWildcardOut(this);
	}

	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitWildcardOut(context, this);
	}

	@Override
	public boolean isValueType() {
		return false;
	}

	@Override
	public ResolvingType resolve() {
		return upperBound.resolve();
	}

	@Override
	public String toString() {
		return "out " + upperBound;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WildcardOutTypeID that = (WildcardOutTypeID) o;
		return Objects.equals(upperBound, that.upperBound);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(upperBound);
	}
}
