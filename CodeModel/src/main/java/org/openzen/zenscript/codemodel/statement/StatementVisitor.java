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
public interface StatementVisitor<T> {
	public T visitBlock(BlockStatement statement);
	
	public T visitBreak(BreakStatement statement);
	
	public T visitContinue(ContinueStatement statement);
	
	public T visitDoWhile(DoWhileStatement statement);
	
	public T visitEmpty(EmptyStatement statement);
	
	public T visitExpression(ExpressionStatement statement);
	
	public T visitForeach(ForeachStatement statement);
	
	public T visitIf(IfStatement statement);
	
	default T visitInvalid(InvalidStatement statement) {
		throw new UnsupportedOperationException("Invalid statement");
	}
	
	public T visitLock(LockStatement statement);
	
	public T visitReturn(ReturnStatement statement);
	
	public T visitSwitch(SwitchStatement statement);
	
	public T visitThrow(ThrowStatement statement);
	
	public T visitTryCatch(TryCatchStatement statement);
	
	public T visitVar(VarStatement statement);
	
	public T visitWhile(WhileStatement statement);
}
