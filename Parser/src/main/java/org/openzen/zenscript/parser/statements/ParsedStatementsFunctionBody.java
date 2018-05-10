/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.linker.StatementScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementsFunctionBody extends ParsedFunctionBody {
	private final ParsedStatement body;
	
	public ParsedStatementsFunctionBody(ParsedStatement body) {
		this.body = body;
	}
	
	@Override
	public Statement compile(StatementScope scope, FunctionHeader header) {
		return body.compile(scope);
	}
}
