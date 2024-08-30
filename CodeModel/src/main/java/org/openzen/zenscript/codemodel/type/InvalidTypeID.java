package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.List;

public class InvalidTypeID implements TypeID {
	public final CodePosition position;
	public final CompileError error;

	public InvalidTypeID(CodePosition position, CompileError error) {
		this.position = position;
		this.error = error;
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		return this;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {

	}

	@Override
	public boolean isValueType() {
		return false;
	}

	@Override
	public boolean isInvalid() {
		return true;
	}

	@Override
	public ResolvedType resolve() {
		return new MemberSet(this);
	}

	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitInvalid(this);
	}

	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitInvalid(context, this);
	}

	@Override
	public String toString() {
		return "invalid";
		//return error.description;
	}
}
