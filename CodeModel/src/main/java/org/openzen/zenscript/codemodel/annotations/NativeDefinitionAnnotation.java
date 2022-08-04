package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;

public class NativeDefinitionAnnotation implements DefinitionAnnotation {
	private final String identifier;

	public NativeDefinitionAnnotation(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return NativeAnnotationDefinition.INSTANCE;
	}

	@Override
	public void apply(HighLevelDefinition definition) {
		definition.setTag(NativeTag.class, new NativeTag(identifier));
	}

	@Override
	public void applyOnSubtype(HighLevelDefinition definition) {
		// this annotation is not inherited
	}

	@Override
	public void serialize(CodeSerializationOutput output, HighLevelDefinition definition, TypeSerializationContext context) {
		output.writeString(identifier);
	}

	public String getIdentifier() {
		return identifier;
	}
}
