package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.expression.InvalidCompilingExpression;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
import java.util.Optional;

public class IterateStatement implements CodeBlockStatement {
	private final CodePosition position;
	private final StatementCompiler compiler;
	private final List<CompilingVariable> loopVariables;
	private final CompilingExpression list;

	public IterateStatement(
			CodePosition position,
			StatementCompiler compiler,
			List<CompilingVariable> loopVariables,
			CompilingExpression list) {
		this.position = position;
		this.compiler = compiler;
		this.loopVariables = loopVariables;
		this.list = list;
	}

	@Override
	public void collect(SSAVariableCollector collector) {
		for (int i = 0; i < loopVariables.size(); i++) {
			final int finali = i;
			CompilingVariable loopVariable = loopVariables.get(i);
			collector.assign(loopVariable.id, () -> new IterationValue(finali));
		}
	}

	@Override
	public void linkVariables(VariableLinker linker) {
		for (CompilingVariable loopVariable : loopVariables)
			loopVariable.ssaCompilingVariable = linker.get(loopVariable.id);
	}

	private class IterationValue implements CompilingExpression {
		private final int index;

		public IterationValue(int index) {
			this.index = index;
		}

		@Override
		public Expression eval() {
			Expression value = list.eval();
			return compiler.resolve(value.type).findIterator(loopVariables.size())
					.<Expression>map(iterator -> new DummyExpression(iterator.getLoopVariableTypes()[index]))
					.orElse(new InvalidExpression(BasicTypeID.UNDETERMINED, new CompileException(position, CompileErrors.noSuchIterator(value.type, loopVariables.size()))));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public boolean canConstructAs(TypeID type) {
			return false;
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
			return new DummyExpression(type);
		}

		@Override
		public void collect(SSAVariableCollector collector) {}

		@Override
		public void linkVariables(VariableLinker linker) {}
	}
}
