package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.List;

/**
 * Represents a wildcard type with an upper bound. (eg. ? extends Number)
 * <p>
 * This is used in the context of type arguments, where the type argument is not known, but it is known that it is a
 * subtype of the given bound.
 */
public class WildcardOutTypeID implements TypeID {
	public final TypeID lowerBound;

	public WildcardOutTypeID(TypeID lowerBound) {
		this.lowerBound = lowerBound;
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		TypeID mappedLowerBound = lowerBound.instance(mapper);
		return mappedLowerBound == lowerBound ? this : new WildcardOutTypeID(mappedLowerBound);
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
		// No members to inherit, since there is no lower bound...
		return MemberSet.create(this).build();
	}
}
