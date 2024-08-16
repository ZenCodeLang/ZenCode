package org.openzen.zenscript.scriptingexample.tests.runner;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class TypeofGlobal implements IGlobal {
	public static final TypeofGlobal INSTANCE = new TypeofGlobal();

	private TypeofGlobal() {}

	@Override
	public CompilableExpression getExpression(CodePosition position, TypeID[] typeArguments) {
		return new Compilable(position);
	}

	@Override
	public Optional<TypeID> getType(CodePosition position, TypeBuilder types, TypeID[] typeArguments) {
		return Optional.empty();
	}

	private static class Compilable implements CompilableExpression {
		private final CodePosition position;

		public Compilable(CodePosition position) {
			this.position = position;
		}

		@Override
		public CodePosition getPosition() {
			return position;
		}

		@Override
		public CompilingExpression compile(ExpressionCompiler compiler) {
			return new Compiling(compiler, position);
		}
	}

	private static class Compiling extends AbstractCompilingExpression implements CompilingCallable {
		public Compiling(ExpressionCompiler compiler, CodePosition position) {
			super(compiler, position);
		}

		@Override
		public Expression eval() {
			return compiler.at(position).invalid(CompileErrors.notAnExpression("typeof is not a valid expression"));
		}

		@Override
		public Optional<CompilingCallable> call() {
			return Optional.of(this);
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			// No-Op since typeof does not create new variables
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			// No-Op since typeof does not create new variables
		}

		@Override
		public Expression call(CodePosition position, CompilingExpression[] arguments) {
			return compiler.at(position).constant(arguments[0].eval().type.toString());
		}

		@Override
		public CastedExpression casted(CodePosition position, CastedEval cast, CompilingExpression[] arguments) {
			return cast.of(call(position, arguments));
		}
	}
}
