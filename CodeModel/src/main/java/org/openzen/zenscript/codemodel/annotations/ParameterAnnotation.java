package org.openzen.zenscript.codemodel.annotations;

public interface ParameterAnnotation {
	ParameterAnnotation[] NONE = new ParameterAnnotation[0];

	AnnotationDefinition getDefinition();

	void apply();
}
