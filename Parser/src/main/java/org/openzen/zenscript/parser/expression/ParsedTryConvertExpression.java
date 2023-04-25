package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.ResultTypeSymbol;

import java.util.Optional;

public class ParsedTryConvertExpression extends ParsedExpression {
	private final CompilableExpression value;

	public ParsedTryConvertExpression(CodePosition position, CompilableExpression value) {
		super(position);

		this.value = value;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, value.compile(compiler));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression value;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression value) {
			super(compiler, position);

			this.value = value;
		}

		@Override
		public Expression eval() {
			Expression value = this.value.eval();
			if (value.thrownType == null)
				return compiler.at(position).invalid(CompileErrors.tryConvertRequiresThrow());

			TypeID type = compiler.types().resultOf(value.type, value.thrownType);
			return cast(cast(type)).value;
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			Optional<DefinitionTypeID> maybeResult = cast.type.simplified().asDefinition();
			if (!maybeResult.isPresent() || maybeResult.get().definition != ResultTypeSymbol.INSTANCE)
				return cast.invalid(CompileErrors.tryConvertWithoutResult());

			DefinitionTypeID result = maybeResult.get();
			Expression value = this.value.cast(cast(result.typeArguments[0])).value;
			if (value.thrownType != null) {
				return cast.of(compiler.at(position).tryConvert(value, result));
			} else {
				return cast.invalid(CompileErrors.tryConvertRequiresThrow());
			}
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			value.collect(collector);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			value.linkVariables(linker);
		}
	}
}
