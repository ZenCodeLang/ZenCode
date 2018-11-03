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
	T visitBlock(BlockStatement statement);
	
	T visitBreak(BreakStatement statement);
	
	T visitContinue(ContinueStatement statement);
	
	T visitDoWhile(DoWhileStatement statement);
	
	T visitEmpty(EmptyStatement statement);
	
	T visitExpression(ExpressionStatement statement);
	
	T visitForeach(ForeachStatement statement);
	
	T visitIf(IfStatement statement);
	
	default T visitInvalid(InvalidStatement statement) {
		throw new UnsupportedOperationException("Invalid statement");
	}
	
	T visitLock(LockStatement statement);
	
	T visitReturn(ReturnStatement statement);
	
	T visitSwitch(SwitchStatement statement);
	
	T visitThrow(ThrowStatement statement);
	
	T visitTryCatch(TryCatchStatement statement);
	
	T visitVar(VarStatement statement);
	
	T visitWhile(WhileStatement statement);
}
