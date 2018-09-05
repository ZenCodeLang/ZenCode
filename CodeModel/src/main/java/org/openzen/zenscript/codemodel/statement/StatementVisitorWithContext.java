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
	public R visitBlock(C context, BlockStatement statement);
	
	public R visitBreak(C context, BreakStatement statement);
	
	public R visitContinue(C context, ContinueStatement statement);
	
	public R visitDoWhile(C context, DoWhileStatement statement);
	
	public R visitEmpty(C context, EmptyStatement statement);
	
	public R visitExpression(C context, ExpressionStatement statement);
	
	public R visitForeach(C context, ForeachStatement statement);
	
	public R visitIf(C context, IfStatement statement);
	
	public R visitLock(C context, LockStatement statement);
	
	public R visitReturn(C context, ReturnStatement statement);
	
	public R visitSwitch(C context, SwitchStatement statement);
	
	public R visitThrow(C context, ThrowStatement statement);
	
	public R visitTryCatch(C context, TryCatchStatement statement);
	
	public R visitVar(C context, VarStatement statement);
	
	public R visitWhile(C context, WhileStatement statement);
}
