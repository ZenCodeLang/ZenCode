package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GenericWildcardCastExpression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
	public Optional<Expression> castImplicitFrom(CodePosition position, Expression value, List<ExpansionSymbol> expansions) {
		return lowerBound.castImplicitTo(position, value, lowerBound, expansions)
				.map(v -> new GenericWildcardCastExpression(position, v, this));
	}

	@Override
	public boolean canCastGenericFrom(TypeID fromType, List<ExpansionSymbol> expansions) {
		return lowerBound.extendsOrImplements(fromType, expansions);
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
