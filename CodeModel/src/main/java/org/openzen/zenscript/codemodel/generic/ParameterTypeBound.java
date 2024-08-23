package org.openzen.zenscript.codemodel.generic;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
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
	public Optional<ResolvedType> resolveMembers(List<ExpansionSymbol> expansions) {
		return Optional.of(type.resolve(expansions));
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
