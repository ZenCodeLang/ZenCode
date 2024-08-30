package org.openzen.zenscript.codemodel.generic;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public final class ParameterSuperBound implements TypeParameterBound {
	public final TypeID type;

	public ParameterSuperBound(TypeID type) {
		this.type = type;
	}

	@Override
	public String getCanonical() {
		return "super|" + type.toString();
	}

	@Override
	public Optional<ResolvedType> resolveMembers() {
		return Optional.empty();
	}

	@Override
	public boolean matches(TypeID type) {
		return type.extendsOrImplements(type);
	}

	@Override
	public TypeParameterBound instance(GenericMapper mapper) {
		TypeID translated = type.instance(mapper);
		if (translated == type)
			return this;

		return new ParameterSuperBound(translated);
	}

	@Override
	public Optional<TypeID> asType() {
		return Optional.empty();
	}

	@Override
	public <T> T accept(GenericParameterBoundVisitor<T> visitor) {
		return visitor.visitSuper(this);
	}

	@Override
	public <C, R> R accept(C context, GenericParameterBoundVisitorWithContext<C, R> visitor) {
		return visitor.visitSuper(context, this);
	}

	@Override
	public boolean isObjectType() {
		return true;
	}
}
