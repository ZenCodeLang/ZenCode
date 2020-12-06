package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;

public class InvalidParameterAnnotation implements ParameterAnnotation {
	public final CodePosition position;
	public final CompileExceptionCode code;
	public final String message;

	public InvalidParameterAnnotation(CodePosition position, CompileExceptionCode code, String message) {
		this.position = position;
		this.code = code;
		this.message = message;
	}

	public InvalidParameterAnnotation(CompileException ex) {
		this.position = ex.position;
		this.code = ex.code;
		this.message = ex.getMessage();
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return InvalidAnnotationDefinition.INSTANCE;
	}

	@Override
	public void apply() {

	}
}
