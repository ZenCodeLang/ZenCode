package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.ResultTypeSymbol;

import java.util.Optional;

public class ParsedTryRethrowExpression extends ParsedExpression {
	private final CompilableExpression source;

	public ParsedTryRethrowExpression(CodePosition position, CompilableExpression source) {
		super(position);

		this.source = source;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, source.compile(compiler));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression value;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression value) {
			super(compiler, position);

			this.value = value;
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			if (compiler.getThrowableType().isPresent()) {
				TypeID result = compiler.types().definitionOf(ResultTypeSymbol.INSTANCE, cast.type, compiler.getThrowableType().get());
				CastedExpression original = value.cast(cast(result));
				return cast.of(original.level, compiler.at(position).tryRethrowAsException(original.value, original.value.type));
			} else {
				Optional<DefinitionTypeID> maybeResult = cast.type.simplified().asDefinition();
				if (!maybeResult.isPresent() || maybeResult.get().definition != ResultTypeSymbol.INSTANCE)
					return cast.invalid(CompileErrors.tryRethrowRequiresResult());

				DefinitionTypeID result = maybeResult.get();
				CastedExpression original = value.cast(cast(result));
				return cast.of(original.level, compiler.at(position).tryRethrowAsResult(original.value, result.typeArguments[0]));
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

		@Override
		public Expression eval() {
			Expression original = value.eval();
			if (compiler.getThrowableType().isPresent()) {
				return compiler.at(position).tryRethrowAsException(original, original.type);
			} else {
				Optional<DefinitionTypeID> maybeResult = original.type.simplified().asDefinition();
				if (!maybeResult.isPresent() || maybeResult.get().definition != ResultTypeSymbol.INSTANCE)
					return compiler.at(position).invalid(CompileErrors.tryRethrowRequiresResult());

				DefinitionTypeID result = maybeResult.get();
				return compiler.at(position).tryRethrowAsResult(cast(result).of(original).value, result.typeArguments[0]);
			}
		}
	}
}
