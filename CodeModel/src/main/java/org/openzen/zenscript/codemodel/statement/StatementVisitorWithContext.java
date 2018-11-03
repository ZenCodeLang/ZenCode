/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

/**
 *
 * @author Hoofdgebruiker
 */
public interface StatementVisitorWithContext<C, R> {
	R visitBlock(C context, BlockStatement statement);
	
	R visitBreak(C context, BreakStatement statement);
	
	R visitContinue(C context, ContinueStatement statement);
	
	R visitDoWhile(C context, DoWhileStatement statement);
	
	R visitEmpty(C context, EmptyStatement statement);
	
	R visitExpression(C context, ExpressionStatement statement);
	
	R visitForeach(C context, ForeachStatement statement);
	
	R visitIf(C context, IfStatement statement);
	
	default R visitInvalid(C context, InvalidStatement statement) {
		throw new UnsupportedOperationException("Invalid statement");
	}
	
	R visitLock(C context, LockStatement statement);
	
	R visitReturn(C context, ReturnStatement statement);
	
	R visitSwitch(C context, SwitchStatement statement);
	
	R visitThrow(C context, ThrowStatement statement);
	
	R visitTryCatch(C context, TryCatchStatement statement);
	
	R visitVar(C context, VarStatement statement);
	
	R visitWhile(C context, WhileStatement statement);
}
