/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public class InvalidStatementAnnotation implements StatementAnnotation {
	public final CodePosition position;
	public final CompileExceptionCode code;
	public final String message;
	
	public InvalidStatementAnnotation(CodePosition position, CompileExceptionCode code, String message) {
		this.position = position;
		this.code = code;
		this.message = message;
	}
	
	public InvalidStatementAnnotation(CompileException ex) {
		this.position = ex.position;
		this.code = ex.code;
		this.message = ex.getMessage();
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return InvalidAnnotationDefinition.INSTANCE;
	}

	@Override
	public Statement apply(Statement statement, StatementScope scope) {
		return statement;
	}
}
