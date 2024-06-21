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

public class 	ParsedExpressionInt extends ParsedExpression {
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
					return cast.of(level(signed >= Byte.MIN_VALUE && signed <= Byte.MAX_VALUE), new ConstantSByteExpression(position, (byte)value));
				case BYTE:
					return cast.of(level(!negative && value <= 0xFF), new ConstantByteExpression(position, (int)value));
				case SHORT:
					return cast.of(level(signed >= Short.MIN_VALUE && signed <= Short.MAX_VALUE), new ConstantShortExpression(position, (short) value));
				case USHORT:
					return cast.of(level(!negative && value <= 0xFFFF), new ConstantUShortExpression(position, (int)value));
				case INT:
					return cast.of(level(signed >= Integer.MIN_VALUE && signed <= Integer.MAX_VALUE), new ConstantIntExpression(position, (int)value));
				case UINT:
					return cast.of(level(!negative && value <= 0xFFFFFFFFL), new ConstantUIntExpression(position, (int)value));
				case USIZE:
					return cast.of(level(!negative && value <= 0xFFFFFFFFL), new ConstantUSizeExpression(position, value));
				case LONG:
					return cast.of(new ConstantLongExpression(position, value));
				case ULONG:
					return cast.of(level(!negative), new ConstantULongExpression(position, value));
				case FLOAT:
					return cast.of(new ConstantFloatExpression(position, value));
				case DOUBLE:
					return cast.of(new ConstantDoubleExpression(position, value));
			}

			return cast.of(eval());
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

		private CastedExpression.Level level(boolean fits) {
			return fits ? CastedExpression.Level.EXACT : CastedExpression.Level.EXPLICIT;
		}
	}

	@Override
	public CompilingSwitchValue compileSwitchValue(ExpressionCompiler compiler) {
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
			return type -> new ErrorSwitchValue(position, CompileErrors.invalidSwitchCaseExpression());

		return type -> new IntSwitchValue((int) value);
	}
}
