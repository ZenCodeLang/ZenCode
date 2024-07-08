package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.ConstantCharExpression;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.CharSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.ErrorSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.StringSwitchValue;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class ParsedExpressionString extends ParsedExpression {
	public final String value;
	public final boolean singleQuote;

	public ParsedExpressionString(CodePosition position, String value, boolean singleQuote) {
		super(position);

		this.value = value;
		this.singleQuote = singleQuote;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, value);
	}

	@Override
	public CompilingSwitchValue compileSwitchValue(ExpressionCompiler compiler) {
		return type -> {
			if (type == BasicTypeID.CHAR) {
				if (value.length() != 1)
					return new ErrorSwitchValue(position, CompileErrors.stringInsteadOfChar());

				return new CharSwitchValue(value.charAt(0));
			} else if (type == BasicTypeID.STRING) {
				return new StringSwitchValue(value);
			} else {
				return new ErrorSwitchValue(position, CompileErrors.stringForNonStringSwitchValue(type));
			}
		};
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final String value;

		public Compiling(ExpressionCompiler compiler, CodePosition position, String value) {
			super(compiler, position);

			this.value = value;
		}

		@Override
		public Expression eval() {
			return new ConstantStringExpression(position, value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			TypeID type = cast.type.simplified();
			boolean literalCanBeChar = value.length() == 1;

			if (type == BasicTypeID.STRING) {
				CastedExpression.Level level = literalCanBeChar ? CastedExpression.Level.WIDENING : CastedExpression.Level.EXACT;
				return cast.of(level, eval());
			} else if (type.simplified() == BasicTypeID.CHAR) {
				if (!literalCanBeChar)
					return cast.invalid(CompileErrors.invalidCharLiteral());

				return cast.of(new ConstantCharExpression(position, value.charAt(0)));
			} else {
				ResolvedType resolvedType = compiler.resolve(type);
				Optional<StaticCallable> constructor = resolvedType.findImplicitConstructor();
				if (constructor.isPresent()) {
					return constructor.get().casted(compiler, position, cast, null, this);
				} else {
					return cast.of(eval());
				}
			}
		}

		@Override
		public void collect(SSAVariableCollector collector) {

		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {

		}
	}
}
