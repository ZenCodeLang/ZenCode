package org.openzen.zenscript.codemodel.statement;

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
