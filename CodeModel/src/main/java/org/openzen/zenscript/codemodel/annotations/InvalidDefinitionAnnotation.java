package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;

public class InvalidDefinitionAnnotation implements DefinitionAnnotation {
	public final CodePosition position;
	public final CompileError error;

	public InvalidDefinitionAnnotation(CodePosition position, CompileError error) {
		this.position = position;
		this.error = error;
	}

	public InvalidDefinitionAnnotation(CompileException ex) {
		this.position = ex.position;
		this.error = ex.error;
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return InvalidAnnotationDefinition.INSTANCE;
	}

	@Override
	public void apply(HighLevelDefinition definition) {

	}

	@Override
	public void applyOnSubtype(HighLevelDefinition definition) {

	}

	@Override
	public void serialize(CodeSerializationOutput output, HighLevelDefinition definition, TypeSerializationContext context) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
