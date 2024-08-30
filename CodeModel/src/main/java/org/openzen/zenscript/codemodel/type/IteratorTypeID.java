package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Arrays;
import java.util.List;

public class IteratorTypeID implements TypeID {
	public final TypeID[] iteratorTypes;

	public IteratorTypeID(TypeID[] iteratorTypes) {
		this.iteratorTypes = iteratorTypes;
	}

	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitIterator(this);
	}

	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitIterator(context, this);
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
	public ResolvingType resolve() {
		return MemberSet.create(this).build(); // no members yet
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		TypeID[] instanced = mapper.map(iteratorTypes);
		return new IteratorTypeID(instanced);
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		for (TypeID type : iteratorTypes)
			type.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 13 * hash + Arrays.deepHashCode(this.iteratorTypes);
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
		final IteratorTypeID other = (IteratorTypeID) obj;
		return Arrays.deepEquals(this.iteratorTypes, other.iteratorTypes);
	}
}
