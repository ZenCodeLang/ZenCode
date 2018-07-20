/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.PrecompilationState;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementsFunctionBody implements ParsedFunctionBody {
	private final ParsedStatement body;
	
	public ParsedStatementsFunctionBody(ParsedStatement body) {
		this.body = body;
	}
	
	@Override
	public Statement compile(StatementScope scope, FunctionHeader header) {
		return body.compile(scope);
	}

	@Override
	public ITypeID precompileForResultType(StatementScope scope, PrecompilationState precompileState) {
		return body.precompileForResultType(scope, precompileState);
	}
}
