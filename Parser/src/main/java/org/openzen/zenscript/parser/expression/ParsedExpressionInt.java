package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.ErrorSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.IntSwitchValue;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ParsedExpressionInt extends ParsedExpression {
	public final boolean negative;
	public final long value;
	public final String suffix;

	public ParsedExpressionInt(CodePosition position, String value) {
		super(position);

		int split = value.length();
		while (isLetter(value.charAt(split - 1)))
			split--;

		negative = value.charAt(0) == '-';
		this.value = Long.parseLong(value.substring(0, split));
		suffix = value.substring(split);
	}

	private ParsedExpressionInt(CodePosition position, boolean negative, long value, String suffix) {
		super(position);

		this.negative = negative;
		this.value = value;
		this.suffix = suffix;
	}

	public static ParsedExpressionInt parsePrefixed(CodePosition position, String value) {
		boolean negative = value.startsWith("-");
		if (negative)
			value = value.substring(1);

		String suffix = "";
		if (value.endsWith("u") || value.endsWith("l") || value.endsWith("U") || value.endsWith("L")) {
			suffix = value.substring(value.length() - 1);
			value = value.substring(0, value.length() - 1);
		} else if (value.endsWith("ul") || value.endsWith("UL")) {
			suffix = value.substring(value.length() - 2);
			value = value.substring(0, value.length() - 2);
		}

		value = value.toLowerCase();

		long parsed = 0;
		if (value.startsWith("0x")) {
			for (char c : value.substring(2).toCharArray()) {
				if (c >= '0' && c <= '9')
					parsed = parsed * 16 + (c - '0');
				else if (c >= 'a' && c <= 'f')
					parsed = parsed * 16 + 10 + (c - 'a');
				else if (c != '_')
					throw new NumberFormatException("Invalid number: " + value);
			}
		} else if (value.startsWith("0b")) {
			for (char c : value.substring(2).toCharArray()) {
				if (c == '0')
					parsed = parsed * 2;
				else if (c == '1')
					parsed = parsed * 2 + 1;
				else if (c != '_')
					throw new NumberFormatException("Invalid number: " + value);
			}
		} else if (value.startsWith("0o")) {
			for (char c : value.substring(2).toCharArray()) {
				if (c >= '0' && c <= '7')
					parsed = parsed * 8 + c - '0';
				else if (c != '_')
					throw new NumberFormatException("Invalid number: " + value);
			}
		} else {
			throw new NumberFormatException("Invalid number: " + value);
		}

		return new ParsedExpressionInt(position, negative, negative ? -parsed : parsed, suffix);
	}

	private static boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, negative, value, suffix);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final boolean negative;
		private final long value;
		private final String suffix;

		public Compiling(ExpressionCompiler compiler, CodePosition position, boolean negative, long value, String suffix) {
			super(compiler, position);
			this.negative = negative;
			this.value = value;
			this.suffix = suffix;
		}

		@Override
		public Expression eval() {
			if (suffix.isEmpty()) {
				if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE)
					return new ConstantIntExpression(position, (int)value);
				else
					return new ConstantLongExpression(position, value);
			} else {
				if (suffix.equals("L") || suffix.equals("l"))
					return new ConstantLongExpression(position, value);
				if (suffix.equals("UL") || suffix.equals("ul"))
					return new ConstantULongExpression(position, value);
				if (suffix.equals("U") || suffix.equals("u"))
					return new ConstantUIntExpression(position, (int)value);
				if (suffix.equals("D") || suffix.equals("d"))
					return new ConstantDoubleExpression(position, value);
				if (suffix.equals("F") || suffix.equals("f"))
					return new ConstantFloatExpression(position, value);

				return compiler.at(position).invalid(CompileErrors.invalidIntSuffix(suffix));
			}
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			TypeID type = cast.type.simplified();

			if (suffix.isEmpty() && isIntegerType(type)) {
				return asInt(cast, (BasicTypeID) type);
			} else if (!suffix.isEmpty()) {
				// Suffix and TypeHint given
				// Check <TypeHint>.<Suffix>(<value>)
				// E.g. 10.0s as TimeSpan -> TimeSpan.s(10.0)
				ResolvedType resolved = compiler.resolve(type);
				Optional<StaticCallable> maybeSuffixConstructor = resolved.findSuffixConstructor(suffix);
				if (maybeSuffixConstructor.isPresent())
					return maybeSuffixConstructor.get().casted(compiler, position, cast, null, this);
			}

			return cast.of(eval());
		}

		@Override
		public void collect(SSAVariableCollector collector) {

		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {

		}

		private CastedExpression asInt(CastedEval cast, BasicTypeID type) {
			long signed = negative ? -value : value;
			switch (type) {
				case SBYTE:
					return castIfFits(cast, signed >= Byte.MIN_VALUE && signed <= Byte.MAX_VALUE, (position, value) -> new ConstantSByteExpression(position, value.byteValue()));
				case BYTE:
					return castIfFits(cast, !negative && value <= 0xFF, (position, value) -> new ConstantByteExpression(position, value.intValue()));
				case SHORT:
					return castIfFits(cast, signed >= Short.MIN_VALUE && signed <= Short.MAX_VALUE, (position, value) -> new ConstantShortExpression(position, value.shortValue()));
				case USHORT:
					return castIfFits(cast, !negative && value <= 0xFFFF, (position, value) -> new ConstantUShortExpression(position, value.intValue()));
				case INT:
					return castIfFits(cast, signed >= Integer.MIN_VALUE && signed <= Integer.MAX_VALUE, (position, value) -> new ConstantIntExpression(position, value.intValue()));
				case UINT:
					return castIfFits(cast, !negative && value <= 0xFFFFFFFFL, (position, value) -> new ConstantUIntExpression(position, value.intValue()));
				case USIZE:
					return castIfFits(cast, !negative && value <= 0xFFFFFFFFL, ConstantUSizeExpression::new);
				case LONG:
					return cast.of(new ConstantLongExpression(position, value));
				case ULONG:
					return castIfFits(cast, !negative, ConstantULongExpression::new);
				case FLOAT:
					return cast.of(new ConstantFloatExpression(position, value));
				case DOUBLE:
					return cast.of(new ConstantDoubleExpression(position, value));
			}

			return cast.of(eval());
		}

		private CastedExpression castIfFits(CastedEval cast, boolean fits, BiFunction<CodePosition, Long, Expression> expr){
			if (fits) {
				return cast.of(CastedExpression.Level.EXACT, expr.apply(this.position, this.value));
			} else if (cast.isExplicit()) {
				return cast.of(CastedExpression.Level.EXPLICIT, expr.apply(this.position, this.value));
			}
			return cast.invalid(CompileErrors.constantSize(this.value,cast.type));
		}

		private boolean isIntegerType(TypeID type) {
			return type == BasicTypeID.BYTE
					|| type == BasicTypeID.SBYTE
					|| type == BasicTypeID.SHORT
					|| type == BasicTypeID.USHORT
					|| type == BasicTypeID.INT
					|| type == BasicTypeID.UINT
					|| type == BasicTypeID.LONG
					|| type == BasicTypeID.ULONG
					|| type == BasicTypeID.USIZE;
		}

	}

	@Override
	public CompilingSwitchValue compileSwitchValue(ExpressionCompiler compiler) {
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
			return type -> new ErrorSwitchValue(position, CompileErrors.invalidSwitchCaseExpression());

		return type -> new IntSwitchValue((int) value);
	}
}
