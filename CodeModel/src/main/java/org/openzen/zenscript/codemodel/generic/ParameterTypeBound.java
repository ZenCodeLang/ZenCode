package org.openzen.zenscript.codemodel.generic;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public final class ParameterTypeBound implements TypeParameterBound {
	public final CodePosition position;
	public final TypeID type;

	public ParameterTypeBound(CodePosition position, TypeID type) {
		this.position = position;
		this.type = type;
	}

	@Override
	public String getCanonical() {
		return type.toString();
	}

	@Override
	public Optional<ResolvingType> resolveMembers() {
		return Optional.of(type.resolve());
	}

	@Override
	public boolean matches(TypeID type) {
		return type.extendsOrImplements(this.type);
	}

	@Override
	public TypeParameterBound instance(GenericMapper mapper) {
		return new ParameterTypeBound(position, type.instance(mapper));
	}

	@Override
	public Optional<TypeID> asType() {
		return Optional.of(type);
	}

	@Override
	public <T> T accept(GenericParameterBoundVisitor<T> visitor) {
		return visitor.visitType(this);
	}

	@Override
	public <C, R> R accept(C context, GenericParameterBoundVisitorWithContext<C, R> visitor) {
		return visitor.visitType(context, this);
	}

	@Override
	public boolean isObjectType() {
		return true;
	}
}
