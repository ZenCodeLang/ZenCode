/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.statements.ParsedStatement;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Stanneke
 */
public class ParsedStatementNull extends ParsedStatement {
	public ParsedStatementNull(CodePosition position) {
		super(position);
	}

	@Override
	public Statement compile(StatementScope scope) {
		return new EmptyStatement(position);
	}
}
