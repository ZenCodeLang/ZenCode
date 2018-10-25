/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.statement.ContinueStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.InvalidStatement;
import org.openzen.zenscript.parser.ParsedAnnotation;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementContinue extends ParsedStatement {
	public final String name;
	
	public ParsedStatementContinue(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String name) {
		super(position, annotations, whitespace);
		
		this.name = name;
	}

	@Override
	public Statement compile(StatementScope scope) {
		LoopStatement target = scope.getLoop(name);
		if (target == null)
			return new InvalidStatement(position, CompileExceptionCode.CONTINUE_OUTSIDE_LOOP, name == null ? "Not in a loop" : "No such loop: " + name);
		return result(new ContinueStatement(position, target), scope);
	}
}
