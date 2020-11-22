package org.openzen.zenscript.codemodel.statement;

import java.util.function.Consumer;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.scope.TypeScope;

public class InvalidStatement extends Statement {
	public final CompileExceptionCode code;
	public final String message;
	
	public InvalidStatement(CodePosition position, CompileExceptionCode code, String message) {
		super(position, null);
		
		this.code = code;
		this.message = message;
	}
	
	public InvalidStatement(CompileException ex) {
		super(ex.position, null);
		
		this.code = ex.code;
		this.message = ex.message;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitInvalid(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitInvalid(context, this);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		
	}

	@Override
	public Statement normalize(TypeScope scope, ConcatMap<LoopStatement, LoopStatement> modified) {
		return this;
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		return this;
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		return this;
	}
}
