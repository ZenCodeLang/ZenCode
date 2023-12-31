package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.List;
import java.util.Optional;

public class ParsedNewExpression extends ParsedExpression {
	private final IParsedType type;
	private final ParsedCallArguments arguments;
	private final List<IParsedType> typeArguments;

	public ParsedNewExpression(CodePosition position, IParsedType type, ParsedCallArguments arguments, List<IParsedType> typeArguments) {
		super(position);

		this.type = type;
		this.arguments = arguments;
        this.typeArguments = typeArguments;
    }

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		TypeID type = this.type.compile(compiler.types());

		TypeID[] compiledTypeArguments = Optional.ofNullable(this.typeArguments)
				.map(typeArguments -> typeArguments.stream()
						.map(parsedType -> parsedType.compile(compiler.types()))
						.toArray(TypeID[]::new)
				).orElse(TypeID.NONE);

		return new Compiling(compiler, position, type, arguments.compile(compiler), compiledTypeArguments);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final TypeID type;
		private final CompilingExpression[] arguments;
		private final TypeID[] typeArguments;

		public Compiling(ExpressionCompiler compiler, CodePosition position, TypeID type, CompilingExpression[] arguments, TypeID[] typeArguments) {
			super(compiler, position);

			this.type = type;
			this.arguments = arguments;
			this.typeArguments = typeArguments;
		}

		@Override
		public Expression eval() {
			return compiler.resolve(type).getConstructor().call(compiler, position, typeArguments, arguments);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			for (CompilingExpression expression : arguments) {
				expression.collect(collector);
			}
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			for (CompilingExpression expression : arguments) {
				expression.linkVariables(linker);
			}
		}
	}
}
