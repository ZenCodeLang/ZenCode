package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;

public class InvalidParameterAnnotation implements ParameterAnnotation {
	public final CodePosition position;
	public final CompileError error;

	public InvalidParameterAnnotation(CodePosition position, CompileError error) {
		this.position = position;
		this.error = error;
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return InvalidAnnotationDefinition.INSTANCE;
	}

	@Override
	public void apply() {

	}
}
