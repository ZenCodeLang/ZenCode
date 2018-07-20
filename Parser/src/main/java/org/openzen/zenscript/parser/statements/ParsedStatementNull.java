/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;

/**
 *
 * @author Stanneke
 */
public class ParsedStatementNull extends ParsedStatement {
	public ParsedStatementNull(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace) {
		super(position, annotations, whitespace);
	}

	@Override
	public Statement compile(StatementScope scope) {
		return result(new EmptyStatement(position), scope);
	}

	@Override
	public ITypeID precompileForResultType(StatementScope scope, PrecompilationState precompileState) {
		return null;
	}
}
