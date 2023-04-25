package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.expression.InvalidCompilingExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.expression.ExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.ExpressionVisitorWithContext;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class IterateStatement implements CodeBlockStatement {
	private final CodePosition position;
	private final VarStatement[] loopVariables;

	public IterateStatement(CodePosition position, VarStatement[] loopVariables) {
		this.position = position;
		this.loopVariables = loopVariables;
	}

	@Override
	public void collect(SSAVariableCollector collector) {
		for (VarStatement loopVariable : loopVariables)
			collector.assign(loopVariable.variable, IterationValue::new);
	}

	@Override
	public void linkVariables(VariableLinker linker) {

	}

	private static class IterationValue implements CompilingExpression {

		@Override
		public Expression eval() {
			throw new UnsupportedOperationException();
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<CompilingCallable> call() {
			return Optional.empty();
		}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Expression as(TypeID type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void collect(SSAVariableCollector collector) {}

		@Override
		public void linkVariables(VariableLinker linker) {}
	}
}
