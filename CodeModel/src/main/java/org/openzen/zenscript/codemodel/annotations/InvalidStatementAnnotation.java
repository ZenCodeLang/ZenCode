package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.statement.Statement;

public class InvalidStatementAnnotation implements StatementAnnotation {
	public final CodePosition position;
	public final CompileError error;

	public InvalidStatementAnnotation(CodePosition position, CompileError error) {
		this.position = position;
		this.error = error;
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return InvalidAnnotationDefinition.INSTANCE;
	}

	@Override
	public Statement apply(Statement statement) {
		return statement;
	}
}
