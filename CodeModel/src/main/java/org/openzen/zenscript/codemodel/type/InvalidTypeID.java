package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.util.List;

public class InvalidTypeID implements TypeID {
	public final CodePosition position;
	public final CompileExceptionCode code;
	public final String message;

	public InvalidTypeID(CodePosition position, CompileExceptionCode code, String message) {
		this.position = position;
		this.code = code;
		this.message = message;
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
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {

	}

	@Override
	public boolean isValueType() {
		return false;
	}

	@Override
	public TypeID getNormalized() {
		return this;
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
	}
}
