package org.openzen.zenscript.scriptingexample;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class TestDefinitionAnnotation implements DefinitionAnnotation {
	private final String value;

	public TestDefinitionAnnotation(String value) {
		this.value = value;
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return TestAnnotationDefinition.INSTANCE;
	}

	@Override
	public void apply(HighLevelDefinition definition) {
		definition.setTag(TestTag.class, new TestTag(value));
		FieldMember field = new FieldMember(
				CodePosition.GENERATED,
				definition,
				Modifiers.PUBLIC,
				"test",
				null,
				BasicTypeID.STRING,
				null,
				null);
		field.initializer = new ConstantStringExpression(CodePosition.GENERATED, value);
		definition.addMember(field);
	}

	@Override
	public void applyOnSubtype(HighLevelDefinition definition) {

	}

	@Override
	public void serialize(CodeSerializationOutput output, HighLevelDefinition definition, TypeSerializationContext context) {

	}
}
