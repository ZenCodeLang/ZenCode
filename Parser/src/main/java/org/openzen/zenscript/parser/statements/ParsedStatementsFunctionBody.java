/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import java.util.List;
import java.util.stream.Collectors;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.linker.StatementScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementsFunctionBody extends ParsedFunctionBody {
	private final List<ParsedStatement> statements;
	
	public ParsedStatementsFunctionBody(List<ParsedStatement> statements) {
		this.statements = statements;
	}
	
	@Override
	public List<Statement> compile(StatementScope scope, FunctionHeader header) {
		return statements.stream()
				.map(s -> s.compile(scope))
				.collect(Collectors.toList());
	}
}
