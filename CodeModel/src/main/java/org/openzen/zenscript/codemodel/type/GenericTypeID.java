package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.codemodel.type.member.ExpandedResolvedType;
import org.openzen.zenscript.codemodel.type.member.ExpandedResolvingType;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenericTypeID implements TypeID {
	public final TypeParameter parameter;

	public GenericTypeID(TypeParameter parameter) {
		this.parameter = parameter;
	}

	public boolean matches(TypeID type) {
		return parameter.matches(type);
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		return mapper.mapGeneric(this);
	}

	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitGeneric(this);
	}

	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitGeneric(context, this);
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
	public boolean isGeneric() {
		return true;
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		if (!typeParameters.contains(parameter))
			typeParameters.add(parameter);
	}

	@Override
	public Optional<GenericTypeID> asGeneric() {
		return Optional.of(this);
	}

	@Override
	public ResolvingType resolve() {
		List<ResolvingType> fromBounds = new ArrayList<>();
		for (TypeParameterBound bound : parameter.bounds) {
			bound.resolveMembers().ifPresent(fromBounds::add);
		}
		return ExpandedResolvingType.of(MemberSet.create(this).build(), fromBounds);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + parameter.hashCode();
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
		final GenericTypeID other = (GenericTypeID) obj;
		return this.parameter == other.parameter;
	}

	@Override
	public String toString() {
		return parameter.toString();
	}
}
