package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.List;
import java.util.Objects;

/**
 * Represents a wildcard type with a lower bound. (e.g. ? super Number)
 */
public class WildcardInTypeID implements TypeID {
	public final TypeID lowerBound;

	public WildcardInTypeID(TypeID lowerBound) {
		this.lowerBound = lowerBound;
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		TypeID mappedLowerBound = lowerBound.instance(mapper);
		return mappedLowerBound == lowerBound ? this : new WildcardInTypeID(mappedLowerBound);
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		lowerBound.extractTypeParameters(typeParameters);
	}

	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitWildcardIn(this);
	}

	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitWildcardIn(context, this);
	}

	@Override
	public boolean isValueType() {
		return false;
	}

	@Override
	public ResolvingType resolve() {
		// No members to inherit, since there is no lower bound...
		return MemberSet.create(this).build();
	}

	@Override
	public String toString() {
		return "in " + lowerBound;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WildcardInTypeID that = (WildcardInTypeID) o;
		return Objects.equals(lowerBound, that.lowerBound);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(lowerBound);
	}
}
