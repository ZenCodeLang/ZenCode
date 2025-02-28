package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.List;

public class WildcardInTypeID implements TypeID {
	public final TypeID upperBound;

	public WildcardInTypeID(TypeID upperBound) {
		this.upperBound = upperBound;
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		TypeID mappedUpperBound = upperBound.instance(mapper);
		return mappedUpperBound == upperBound ? this : new WildcardInTypeID(mappedUpperBound);
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
}
