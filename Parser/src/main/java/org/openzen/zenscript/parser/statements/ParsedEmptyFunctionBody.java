/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedEmptyFunctionBody extends ParsedFunctionBody {
	public final CodePosition position;
	
	public ParsedEmptyFunctionBody(CodePosition position) {
		this.position = position;
	}
	
	@Override
	public Statement compile(StatementScope scope, FunctionHeader header) {
		return new EmptyStatement(position);
	}
}
