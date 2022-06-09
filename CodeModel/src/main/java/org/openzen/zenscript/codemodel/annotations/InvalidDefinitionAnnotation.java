package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;

public class InvalidDefinitionAnnotation implements DefinitionAnnotation {
	public final CodePosition position;
	public final CompileExceptionCode code;
	public final String message;

	public InvalidDefinitionAnnotation(CodePosition position, CompileExceptionCode code, String message) {
		this.position = position;
		this.code = code;
		this.message = message;
	}

	public InvalidDefinitionAnnotation(CompileException ex) {
		this.position = ex.position;
		this.code = ex.code;
		this.message = ex.getMessage();
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
	public void serialize(CodeSerializationOutput output, HighLevelDefinition definition, TypeContext context) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
