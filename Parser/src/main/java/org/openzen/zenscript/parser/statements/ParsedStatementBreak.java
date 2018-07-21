/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.statement.BreakStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementBreak extends ParsedStatement {
	public final String name;
	
	public ParsedStatementBreak(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String name) {
		super(position, annotations, whitespace);
		
		this.name = name;
	}

	@Override
	public Statement compile(StatementScope scope) {
		LoopStatement target = scope.getLoop(name);
		if (target == null)
			throw new CompileException(position, CompileExceptionCode.BREAK_OUTSIDE_LOOP, name == null ? "Not in a loop" : "No such loop: " + name);
		return result(new BreakStatement(position, target), scope);
	}

	@Override
	public ITypeID precompileForResultType(StatementScope scope, PrecompilationState precompileState) {
		return null;
	}
}
