package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;

public interface DefinitionAnnotation {
	DefinitionAnnotation[] NONE = new DefinitionAnnotation[0];

	AnnotationDefinition getDefinition();

	void apply(HighLevelDefinition definition);

	void applyOnSubtype(HighLevelDefinition definition);

	void serialize(CodeSerializationOutput output, HighLevelDefinition definition, TypeSerializationContext context);
}
