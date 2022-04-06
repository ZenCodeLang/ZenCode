package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.IntSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.expression.TypeMatch;
import org.openzen.zenscript.compiler.types.ResolvedType;

import java.util.Collections;
import java.util.Optional;

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
		public Expression as(TypeID type) {
			return null;
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			long signed = negative ? -value : value;
			if (suffix.isEmpty() && (returnType instanceof BasicTypeID)) {
				switch ((BasicTypeID) returnType) {
					case SBYTE:
						return signed >= Byte.MIN_VALUE && signed <= Byte.MAX_VALUE ? TypeMatch.EXACT : TypeMatch.NONE;
					case BYTE:
						return !negative && value <= 0xFF ? TypeMatch.EXACT : TypeMatch.NONE;
					case SHORT:
						return signed >= Short.MIN_VALUE && signed <= Short.MAX_VALUE ? TypeMatch.EXACT : TypeMatch.NONE;
					case USHORT:
						return !negative && value <= 0xFFFF ? TypeMatch.EXACT : TypeMatch.NONE;
					case INT:
						return signed >= Integer.MIN_VALUE && signed <= Integer.MAX_VALUE ? TypeMatch.EXACT : TypeMatch.NONE;
					case UINT:
					case USIZE:
						return !negative && value <= 0xFFFFFFFFL ? TypeMatch.EXACT : TypeMatch.NONE;
					case LONG:
						return TypeMatch.EXACT;
					case ULONG:
						return !negative ? TypeMatch.EXACT : TypeMatch.NONE;
					default:
						return TypeMatch.NONE;
				}
			} else if (!suffix.isEmpty()) {
				// Suffix and TypeHint given
				// Check <TypeHint>.<Suffix>(<value>)
				// E.g. 10.0s as TimeSpan -> TimeSpan.s(10.0)
				ResolvedType type = compiler.resolve(returnType);
				if (type.findStaticMethod(suffix, method -> method.isImplicit() && method.isCompatible(returnType, this::isIntegerType)).isPresent())
					return TypeMatch.EXACT;

				if (suffix.equals("L") || suffix.equals("l"))
					return compiler.matchType() InferredType.success(BasicTypeID.LONG);
				if (suffix.equals("UL") || suffix.equals("ul"))
					return InferredType.success(BasicTypeID.ULONG);
				if (suffix.equals("U") || suffix.equals("u"))
					return InferredType.success(BasicTypeID.UINT);
				if (suffix.equals("D") || suffix.equals("d"))
					return InferredType.success(BasicTypeID.DOUBLE);
				if (suffix.equals("F") || suffix.equals("f"))
					return InferredType.success(BasicTypeID.FLOAT);

				if (suffix.equals("f") || suffix.equals("F"))
					return compiler.matchType(BasicTypeID.FLOAT, returnType);
				else if (suffix.equals("d") || suffix.equals("D"))
					return compiler.matchType(BasicTypeID.DOUBLE, returnType);
				else
					return TypeMatch.NONE;


				TypeMembers members = scope.getTypeMembers(hint);
				FunctionalMemberRef method = members.getOrCreateGroup(suffix, true).getStaticMethod(1, hint);
				if (method != null) {
					try {
						ParsedCallArguments parsedArguments = new ParsedCallArguments(Collections.singletonList(new ParsedExpressionInt(position, negative, value, "")));
						CallArguments arguments = parsedArguments.compileCall(position, scope, TypeID.NONE, method.getHeader());
						method.callStatic(position, hint, method.getHeader(), arguments, scope);
					} catch (CompileException ex) {
						return new InvalidExpression(hint, ex);
					}
				}
			}
		}

		private boolean isIntegerType(TypeID type) {
			return type == BasicTypeID.BYTE
					|| type == BasicTypeID.SBYTE
					|| type == BasicTypeID.SHORT
					|| type == BasicTypeID.USHORT
					|| type == BasicTypeID.INT
					|| type == BasicTypeID.UINT
					|| type == BasicTypeID.LONG
					|| type == BasicTypeID.ULONG;
		}

		@Override
		public InferredType inferType() {
			if (suffix.equals("L") || suffix.equals("l"))
				return InferredType.success(BasicTypeID.LONG);
			if (suffix.equals("UL") || suffix.equals("ul"))
				return InferredType.success(BasicTypeID.ULONG);
			if (suffix.equals("U") || suffix.equals("u"))
				return InferredType.success(BasicTypeID.UINT);
			if (suffix.equals("D") || suffix.equals("d"))
				return InferredType.success(BasicTypeID.DOUBLE);
			if (suffix.equals("F") || suffix.equals("f"))
				return InferredType.success(BasicTypeID.FLOAT);

			if (suffix.isEmpty()) {
				if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE)
					return InferredType.success(BasicTypeID.INT);
				else
					return InferredType.success(BasicTypeID.LONG);
			} else {
				return InferredType.failure(CompileExceptionCode.INVALID_SUFFIX, "Invalid suffix: " + suffix);
			}
		}

		private Optional<BasicTypeID> getTypeFromSuffix() {
			if (suffix.equals("L") || suffix.equals("l"))
				return Optional.of(BasicTypeID.LONG);
			if (suffix.equals("UL") || suffix.equals("ul"))
				return Optional.of(BasicTypeID.ULONG);
			if (suffix.equals("U") || suffix.equals("u"))
				return Optional.of(BasicTypeID.UINT);
			if (suffix.equals("D") || suffix.equals("d"))
				return Optional.of(BasicTypeID.DOUBLE);
			if (suffix.equals("F") || suffix.equals("f"))
				return Optional.of(BasicTypeID.FLOAT);

			return Optional.empty();
		}
	}

	@Override
	public Expression compile(ExpressionScope scope) throws CompileException {
		if (suffix.equals("L") || suffix.equals("l"))
			return new ConstantLongExpression(position, value);
		if (suffix.equals("UL") || suffix.equals("ul"))
			return new ConstantULongExpression(position, value);
		if (suffix.equals("U") || suffix.equals("u"))
			return new ConstantUIntExpression(position, (int) value);
		if (suffix.equals("D") || suffix.equals("d"))
			return new ConstantDoubleExpression(position, value);
		if (suffix.equals("F") || suffix.equals("f"))
			return new ConstantFloatExpression(position, value);

		for (TypeID hint : scope.hints) {
			if (suffix.isEmpty() && (hint instanceof BasicTypeID)) {
				switch ((BasicTypeID) hint) {
					case SBYTE:
						return new ConstantSByteExpression(position, (byte) value);
					case BYTE:
						if (negative)
							break;

						return new ConstantByteExpression(position, (int) (value & 0xFF));
					case SHORT:
						return new ConstantShortExpression(position, (short) value);
					case USHORT:
						if (negative)
							break;

						return new ConstantUShortExpression(position, (int) (value & 0xFFFF));
					case INT:
						return new ConstantIntExpression(position, (int) value);
					case UINT:
						if (negative)
							break;

						return new ConstantUIntExpression(position, (int) value);
					case LONG:
						return new ConstantLongExpression(position, value);
					case ULONG:
						if (negative)
							break;

						return new ConstantULongExpression(position, value);
					case USIZE:
						if (negative)
							break;

						return new ConstantUSizeExpression(position, value);
					case CHAR:
						if (negative)
							break;

						return new ConstantCharExpression(position, (char) value);
					default:
				}
			} else if (!suffix.isEmpty()) {
				TypeMembers members = scope.getTypeMembers(hint);
				FunctionalMemberRef method = members.getOrCreateGroup(suffix, true).getStaticMethod(1, hint);
				if (method != null) {
					try {
						ParsedCallArguments parsedArguments = new ParsedCallArguments(Collections.singletonList(new ParsedExpressionInt(position, negative, value, "")));
						CallArguments arguments = parsedArguments.compileCall(position, scope, TypeID.NONE, method.getHeader());
						method.callStatic(position, hint, method.getHeader(), arguments, scope);
					} catch (CompileException ex) {
						return new InvalidExpression(hint, ex);
					}
				}
			}
		}

		if (suffix.isEmpty()) {
			if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE)
				return new ConstantIntExpression(position, (int) value);
			else
				return new ConstantLongExpression(position, value);
		} else {
			throw new CompileException(position, CompileExceptionCode.INVALID_SUFFIX, "Invalid suffix: " + suffix);
		}
	}

	@Override
	public SwitchValue compileToSwitchValue(TypeID type, ExpressionScope scope) throws CompileException {
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
			throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "value is too large for a switch case");

		return new IntSwitchValue((int) value);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
