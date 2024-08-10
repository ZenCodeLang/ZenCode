package org.openzen.zenscript.scriptingexample.tests.runner;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.scriptingexample.TestTag;

import java.util.Optional;

public class TestAnnotationValueGlobal implements IGlobal {
	public static final TestAnnotationValueGlobal INSTANCE = new TestAnnotationValueGlobal();

	private TestAnnotationValueGlobal() {}

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
			return compiler.at(position).invalid(CompileErrors.notAnExpression("testAnnotationValue is not a valid expression"));
		}

		@Override
		public Optional<CompilingCallable> call() {
			return Optional.of(this);
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			// No-Op since testAnnotationValue does not create new variables
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			// No-Op since testAnnotationValue does not create new variables
		}

		@Override
		public Expression call(CodePosition position, CompilingExpression[] arguments) {
			return compiler.at(position).constant(arguments[0].eval().type.asDefinition().map(definition -> {
				if (definition.definition instanceof HighLevelDefinition) {
					TestTag tag = ((HighLevelDefinition)definition.definition).getTag(TestTag.class);
					return tag == null ? "null" : tag.value;
				} else {
					return "null";
				}
			}).orElse("invalid"));
		}

		@Override
		public CastedExpression casted(CodePosition position, CastedEval cast, CompilingExpression[] arguments) {
			return cast.of(call(position, arguments));
		}
	}
}
