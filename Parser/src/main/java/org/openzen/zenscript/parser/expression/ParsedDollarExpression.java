package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedDollarExpression extends ParsedExpression {
	public ParsedDollarExpression(CodePosition position) {
		super(position);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position);
	}

	private static class Compiling extends AbstractCompilingExpression {
		public Compiling(ExpressionCompiler compiler, CodePosition position) {
			super(compiler, position);
		}

		@Override
		public Expression eval() {
			return compiler.dollar().map(CompilingExpression::eval)
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noDollarHere()));
		}

		@Override
		public void collect(SSAVariableCollector collector) {

		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {

		}
	}
}
