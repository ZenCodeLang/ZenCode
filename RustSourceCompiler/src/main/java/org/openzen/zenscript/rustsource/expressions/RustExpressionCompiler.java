package org.openzen.zenscript.rustsource.expressions;

import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.formattershared.ExpressionString;
import org.openzen.zenscript.formattershared.FormattableOperator;
import org.openzen.zenscript.rustsource.compiler.ImportSet;
import org.openzen.zenscript.rustsource.definitions.RustModule;
import org.openzen.zenscript.rustsource.types.RustTypeCompiler;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RustExpressionCompiler {
	private final ImportSet imports;
	private final boolean multithreaded;

	public RustExpressionCompiler(ImportSet imports, boolean multithreaded) {
		this.imports = imports;
		this.multithreaded = multithreaded;
	}

	public String compile(Expression expression) {
		return expression.accept(new Visitor()).value;
	}

	private class Visitor implements ExpressionVisitor<ExpressionString> {

		@Override
		public ExpressionString visitAndAnd(AndAndExpression expression) {
			return binary(expression.left, expression.right, RustOperator.ANDAND);
		}

		@Override
		public ExpressionString visitArray(ArrayExpression expression) {
			return new ExpressionString(
					"vec!(" + Arrays.stream(expression.expressions).map(x -> x.accept(this).value).collect(Collectors.joining(", ")) + ")",
					RustOperator.CALL
			);
		}

		@Override
		public ExpressionString visitCompare(CompareExpression expression) {
			return binary(expression.left, expression.right, RustOperator.getComparison(expression.comparison));
		}

		@Override
		public ExpressionString visitCall(CallExpression expression) {
			if (expression.member.getBuiltin() != null) {
				return visitBuiltin(expression);
			} else {
				StringBuilder result = new StringBuilder();
				result.append(expression.target.accept(this));
				result.append(expression.member.getMethodName());
				result.append("(");
				boolean first = true;
				for (Expression argument : expression.arguments.arguments) {
					if (first) {
						first = false;
					} else {
						result.append(", ");
					}
					result.append(argument.accept(this));
				}
				result.append(")");
				return new ExpressionString(result.toString(), RustOperator.CALL);
			}
		}

		@Override
		public ExpressionString visitCallStatic(CallStaticExpression expression) {
			StringBuilder result = new StringBuilder();
			result.append(new RustTypeCompiler(imports, multithreaded).compile(expression.target));
			result.append("::").append(expression.member.getMethodName());
			result.append("(");
			boolean first = true;
			for (Expression argument : expression.arguments.arguments) {
				if (first) {
					first = false;
				} else {
					result.append(", ");
				}
				result.append(argument.accept(this));
			}
			result.append(")");
			return new ExpressionString(result.toString(), RustOperator.CALL);
		}

		@Override
		public ExpressionString visitCapturedClosure(CapturedClosureExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitCapturedDirect(CapturedDirectExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitCapturedParameter(CapturedParameterExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitCapturedThis(CapturedThisExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitCast(CastExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitCheckNull(CheckNullExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitCoalesce(CoalesceExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitConditional(ConditionalExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitConst(ConstExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitConstantBool(ConstantBoolExpression expression) {
			return new ExpressionString(expression.value ? "true" : "false", RustOperator.PRIMARY);
		}

		@Override
		public ExpressionString visitConstantByte(ConstantByteExpression expression) {
			return new ExpressionString(Integer.toString(expression.value), RustOperator.PRIMARY);
		}

		@Override
		public ExpressionString visitConstantChar(ConstantCharExpression expression) {
			// TODO: escape
			return new ExpressionString("'" + expression.value +  "'", RustOperator.PRIMARY);
		}

		@Override
		public ExpressionString visitConstantDouble(ConstantDoubleExpression expression) {
			return new ExpressionString(Double.toString(expression.value), RustOperator.PRIMARY);
		}

		@Override
		public ExpressionString visitConstantFloat(ConstantFloatExpression expression) {
			return new ExpressionString(Float.toString(expression.value), RustOperator.PRIMARY);
		}

		@Override
		public ExpressionString visitConstantInt(ConstantIntExpression expression) {
			return new ExpressionString(Integer.toString(expression.value), RustOperator.PRIMARY);
		}

		@Override
		public ExpressionString visitConstantLong(ConstantLongExpression expression) {
			return new ExpressionString(Long.toString(expression.value), RustOperator.PRIMARY);
		}

		@Override
		public ExpressionString visitConstantSByte(ConstantSByteExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitConstantShort(ConstantShortExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitConstantString(ConstantStringExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitConstantUInt(ConstantUIntExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitConstantULong(ConstantULongExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitConstantUShort(ConstantUShortExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitConstantUSize(ConstantUSizeExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitConstructorThisCall(ConstructorThisCallExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitEnumConstant(EnumConstantExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitFunction(FunctionExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitGetField(GetFieldExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitGetFunctionParameter(GetFunctionParameterExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitGetLocalVariable(GetLocalVariableExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitGetMatchingVariantField(GetMatchingVariantField expression) {
			return null;
		}

		@Override
		public ExpressionString visitGetStaticField(GetStaticFieldExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitGetter(GetterExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitGlobal(GlobalExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitGlobalCall(GlobalCallExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitInterfaceCast(InterfaceCastExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitIs(IsExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitMakeConst(MakeConstExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitMap(MapExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitMatch(MatchExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitNew(NewExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitNull(NullExpression expression) {
			return new ExpressionString(
					imports.addImport(RustModule.STD_OPTION, "Option") + "::None",
					RustOperator.PRIMARY);
		}

		@Override
		public ExpressionString visitOrOr(OrOrExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitPanic(PanicExpression expression) {
			throw new UnsupportedOperationException("not yet supported");
		}

		@Override
		public ExpressionString visitPlatformSpecific(Expression expression) {
			throw new UnsupportedOperationException("not yet supported");
		}

		@Override
		public ExpressionString visitPostCall(PostCallExpression expression) {
			throw new UnsupportedOperationException("not yet supported");
		}

		@Override
		public ExpressionString visitRange(RangeExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitSameObject(SameObjectExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitSetField(SetFieldExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitSetFunctionParameter(SetFunctionParameterExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitSetLocalVariable(SetLocalVariableExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitSetStaticField(SetStaticFieldExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitSetter(SetterExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitStaticGetter(StaticGetterExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitStaticSetter(StaticSetterExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitSupertypeCast(SupertypeCastExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitSubtypeCast(SubtypeCastExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitThis(ThisExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitThrow(ThrowExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitTryConvert(TryConvertExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitVariantValue(VariantValueExpression expression) {
			return null;
		}

		@Override
		public ExpressionString visitWrapOptional(WrapOptionalExpression expression) {
			return null;
		}

		private ExpressionString visitBuiltin(CallExpression expression) {
			Expression[] arguments = expression.arguments.arguments;
			switch (expression.member.getBuiltin()) {
				case BOOL_NOT:
					return unaryPrefix(expression, RustOperator.NOT);
				case BOOL_AND:
					return binary(expression, RustOperator.AND);
				case BOOL_OR:
					return binary(expression, RustOperator.OR);
				case BOOL_XOR:
					return binary(expression, RustOperator.XOR);
				case BOOL_EQUALS:
					return binary(expression, RustOperator.EQUALS);
				case BOOL_NOTEQUALS:
					return binary(expression, RustOperator.NOTEQUALS);
				case BYTE_NOT:
					return unaryPrefix(expression, RustOperator.INVERT);
				case BYTE_ADD_BYTE:
					return binary(expression, RustOperator.ADD);
				case BYTE_SUB_BYTE:
					return binary(expression, RustOperator.SUB);
				case BYTE_MUL_BYTE:
					return binary(expression, RustOperator.MUL);
				case BYTE_DIV_BYTE:
					return binary(expression, RustOperator.DIV);
				case BYTE_MOD_BYTE:
					return binary(expression, RustOperator.MOD);
				case BYTE_AND_BYTE:
					return binary(expression, RustOperator.AND);
				case BYTE_OR_BYTE:
					return binary(expression, RustOperator.OR);
				case BYTE_XOR_BYTE:
					return binary(expression, RustOperator.XOR);
				case BYTE_SHL:
					return binary(expression, RustOperator.SHL);
				case BYTE_SHR:
					return binary(expression, RustOperator.SHR);
				case SBYTE_NOT:
					return unaryPrefix(expression, RustOperator.INVERT);
				case SBYTE_NEG:
					return unaryPrefix(expression, RustOperator.NEG);
				case SBYTE_ADD_SBYTE:
					return binary(expression, RustOperator.ADD);
				case SBYTE_SUB_SBYTE:
					return binary(expression, RustOperator.SUB);
				case SBYTE_MUL_SBYTE:
					return binary(expression, RustOperator.MUL);
				case SBYTE_DIV_SBYTE:
					return binary(expression, RustOperator.DIV);
				case SBYTE_MOD_SBYTE:
					return binary(expression, RustOperator.MOD);
				case SBYTE_AND_SBYTE:
					return binary(expression, RustOperator.AND);
				case SBYTE_OR_SBYTE:
					return binary(expression, RustOperator.OR);
				case SBYTE_XOR_SBYTE:
					return binary(expression, RustOperator.XOR);
				case SBYTE_SHL:
					return binary(expression, RustOperator.SHL);
				case SBYTE_SHR:
					return binary(expression, RustOperator.SHR);
				case SBYTE_USHR:
					return binary(expression, RustOperator.USHR);
				case SHORT_NOT:
					return unaryPrefix(expression, RustOperator.INVERT);
				case SHORT_NEG:
					return unaryPrefix(expression, RustOperator.NEG);
				case SHORT_ADD_SHORT:
					return binary(expression, RustOperator.ADD);
				case SHORT_SUB_SHORT:
					return binary(expression, RustOperator.SUB);
				case SHORT_MUL_SHORT:
					return binary(expression, RustOperator.MUL);
				case SHORT_DIV_SHORT:
					return binary(expression, RustOperator.DIV);
				case SHORT_MOD_SHORT:
					return binary(expression, RustOperator.MOD);
				case SHORT_AND_SHORT:
					return binary(expression, RustOperator.AND);
				case SHORT_OR_SHORT:
					return binary(expression, RustOperator.OR);
				case SHORT_XOR_SHORT:
					return binary(expression, RustOperator.XOR);
				case SHORT_SHL:
					return binary(expression, RustOperator.SHL);
				case SHORT_SHR:
					return binary(expression, RustOperator.SHR);
				case USHORT_NOT:
					return unaryPrefix(expression, RustOperator.INVERT);
				case USHORT_ADD_USHORT:
					return binary(expression, RustOperator.ADD);
				case USHORT_SUB_USHORT:
					return binary(expression, RustOperator.SUB);
				case USHORT_MUL_USHORT:
					return binary(expression, RustOperator.MUL);
				case USHORT_DIV_USHORT:
					return binary(expression, RustOperator.DIV);
				case USHORT_MOD_USHORT:
					return binary(expression, RustOperator.MOD);
				case USHORT_AND_USHORT:
					return binary(expression, RustOperator.AND);
				case USHORT_OR_USHORT:
					return binary(expression, RustOperator.OR);
				case USHORT_XOR_USHORT:
					return binary(expression, RustOperator.XOR);
				case USHORT_SHL:
					return binary(expression, RustOperator.SHL);
				case USHORT_SHR:
					return binary(expression, RustOperator.SHR);
				case INT_NOT:
					return unaryPrefix(expression, RustOperator.INVERT);
				case INT_NEG:
					return unaryPrefix(expression, RustOperator.NEG);
				case INT_ADD_INT:
					return binary(expression, RustOperator.ADD);
				case INT_SUB_INT:
					return binary(expression, RustOperator.SUB);
				case INT_MUL_INT:
					return binary(expression, RustOperator.MUL);
				case INT_DIV_INT:
					return binary(expression, RustOperator.DIV);
				case INT_MOD_INT:
					return binary(expression, RustOperator.MOD);
				case INT_AND_INT:
					return binary(expression, RustOperator.AND);
				case INT_OR_INT:
					return binary(expression, RustOperator.OR);
				case INT_XOR_INT:
					return binary(expression, RustOperator.XOR);
				case INT_SHL:
					return binary(expression, RustOperator.SHL);
				case INT_SHR:
					return binary(expression, RustOperator.SHR);
				case INT_USHR:
					return binary(expression, RustOperator.USHR);
				case INT_COUNT_LOW_ZEROES:
					return call(expression, ".trailing_zeros()");
				case INT_COUNT_HIGH_ZEROES:
					return call(expression, ".leading_zeros()");
				case INT_COUNT_LOW_ONES:
					return call(expression, ".trailing_ones()");
				case INT_COUNT_HIGH_ONES:
					return call(expression, ".leading_ones()");
				case UINT_NOT:
					return unaryPrefix(expression, RustOperator.INVERT);
				case UINT_ADD_UINT:
					return binary(expression, RustOperator.ADD);
				case UINT_SUB_UINT:
					return binary(expression, RustOperator.SUB);
				case UINT_MUL_UINT:
					return binary(expression, RustOperator.MUL);
				case UINT_DIV_UINT:
					return binary(expression, RustOperator.DIV);
				case UINT_MOD_UINT:
					return binary(expression, RustOperator.MOD);
				case UINT_AND_UINT:
					return binary(expression, RustOperator.AND);
				case UINT_OR_UINT:
					return binary(expression, RustOperator.OR);
				case UINT_XOR_UINT:
					return binary(expression, RustOperator.XOR);
				case UINT_SHL:
					return binary(expression, RustOperator.SHL);
				case UINT_SHR:
					return binary(expression, RustOperator.USHR);
				case UINT_COUNT_LOW_ZEROES:
					return call(expression, ".trailing_zeros()");
				case UINT_COUNT_HIGH_ZEROES:
					return call(expression, ".leading_zeros()");
				case UINT_COUNT_LOW_ONES:
					return call(expression, ".trailing_ones()");
				case UINT_COUNT_HIGH_ONES:
					return call(expression, ".leading_ones()");
				case LONG_NOT:
					return unaryPrefix(expression, RustOperator.INVERT);
				case LONG_NEG:
					return unaryPrefix(expression, RustOperator.NEG);
				case LONG_ADD_LONG:
					return binary(expression, RustOperator.ADD);
				case LONG_SUB_LONG:
					return binary(expression, RustOperator.SUB);
				case LONG_MUL_LONG:
					return binary(expression, RustOperator.MUL);
				case LONG_DIV_LONG:
					return binary(expression, RustOperator.DIV);
				case LONG_MOD_LONG:
					return binary(expression, RustOperator.MOD);
				case LONG_AND_LONG:
					return binary(expression, RustOperator.AND);
				case LONG_OR_LONG:
					return binary(expression, RustOperator.OR);
				case LONG_XOR_LONG:
					return binary(expression, RustOperator.XOR);
				case LONG_SHL:
					return binary(expression, RustOperator.SHL);
				case LONG_SHR:
					return binary(expression, RustOperator.SHR);
				case LONG_USHR:
					return binary(expression, RustOperator.USHR);
				case LONG_COUNT_LOW_ZEROES:
					return call(expression, ".trailing_zeros()");
				case LONG_COUNT_HIGH_ZEROES:
					return call(expression, ".leading_zeros()");
				case LONG_COUNT_LOW_ONES:
					return call(expression, ".trailing_ones()");
				case LONG_COUNT_HIGH_ONES:
					return call(expression, ".leading_ones()");
				case ULONG_NOT:
					return unaryPrefix(expression, RustOperator.INVERT);
				case ULONG_ADD_ULONG:
					return binary(expression, RustOperator.ADD);
				case ULONG_SUB_ULONG:
					return binary(expression, RustOperator.SUB);
				case ULONG_MUL_ULONG:
					return binary(expression, RustOperator.MUL);
				case ULONG_DIV_ULONG:
					return binary(expression, RustOperator.DIV);
				case ULONG_MOD_ULONG:
					return binary(expression, RustOperator.MOD);
				case ULONG_AND_ULONG:
					return binary(expression, RustOperator.AND);
				case ULONG_OR_ULONG:
					return binary(expression, RustOperator.OR);
				case ULONG_XOR_ULONG:
					return binary(expression, RustOperator.XOR);
				case ULONG_SHL:
					return binary(expression, RustOperator.SHL);
				case ULONG_SHR:
					return binary(expression, RustOperator.USHR);
				case ULONG_COUNT_LOW_ZEROES:
					return call(expression, ".trailing_zeros()");
				case ULONG_COUNT_HIGH_ZEROES:
					return call(expression, ".leading_zeros()");
				case ULONG_COUNT_LOW_ONES:
					return call(expression, ".trailing_ones()");
				case ULONG_COUNT_HIGH_ONES:
					return call(expression, ".leading_ones()");
				case USIZE_NOT:
					return unaryPrefix(expression, RustOperator.INVERT);
				case USIZE_ADD_USIZE:
					return binary(expression, RustOperator.ADD);
				case USIZE_SUB_USIZE:
					return binary(expression, RustOperator.SUB);
				case USIZE_MUL_USIZE:
					return binary(expression, RustOperator.MUL);
				case USIZE_DIV_USIZE:
					return binary(expression, RustOperator.DIV);
				case USIZE_MOD_USIZE:
					return binary(expression, RustOperator.MOD);
				case USIZE_AND_USIZE:
					return binary(expression, RustOperator.AND);
				case USIZE_OR_USIZE:
					return binary(expression, RustOperator.OR);
				case USIZE_XOR_USIZE:
					return binary(expression, RustOperator.XOR);
				case USIZE_SHL:
					return binary(expression, RustOperator.SHL);
				case USIZE_SHR:
					return binary(expression, RustOperator.USHR);
				case USIZE_COUNT_LOW_ZEROES:
					return call(expression, ".trailing_zeros()");
				case USIZE_COUNT_HIGH_ZEROES:
					return call(expression, ".leading_zeros()");
				case USIZE_COUNT_LOW_ONES:
					return call(expression, ".trailing_ones()");
				case USIZE_COUNT_HIGH_ONES:
					return call(expression, ".leading_ones()");
				case FLOAT_NEG:
					return unaryPrefix(expression, RustOperator.NEG);
				case FLOAT_ADD_FLOAT:
					return binary(expression, RustOperator.ADD);
				case FLOAT_SUB_FLOAT:
					return binary(expression, RustOperator.SUB);
				case FLOAT_MUL_FLOAT:
					return binary(expression, RustOperator.MUL);
				case FLOAT_DIV_FLOAT:
					return binary(expression, RustOperator.DIV);
				case FLOAT_MOD_FLOAT:
					return binary(expression, RustOperator.MOD);
				case DOUBLE_NEG:
					return unaryPrefix(expression, RustOperator.NEG);
				case DOUBLE_ADD_DOUBLE:
					return binary(expression, RustOperator.ADD);
				case DOUBLE_SUB_DOUBLE:
					return binary(expression, RustOperator.SUB);
				case DOUBLE_MUL_DOUBLE:
					return binary(expression, RustOperator.MUL);
				case DOUBLE_DIV_DOUBLE:
					return binary(expression, RustOperator.DIV);
				case DOUBLE_MOD_DOUBLE:
					return binary(expression, RustOperator.MOD);
				case CHAR_ADD_INT: {
					ExpressionString left = new ExpressionString(expression.target.accept(this).wrapLeft(RustOperator.CAST) + " as i32", RustOperator.CAST);
					ExpressionString right = expression.arguments.arguments[0].accept(this);
					return ExpressionString.binary(left, right, RustOperator.ADD);
				}
				case CHAR_SUB_INT:
					throw new UnsupportedOperationException();
					//return cast(binary(expression, RustOperator.SUB), "char");
				case CHAR_SUB_CHAR:
					return binary(expression, RustOperator.SUB);
				case CHAR_REMOVE_DIACRITICS:
					throw new UnsupportedOperationException("Not yet supported!");
				case CHAR_TO_LOWER_CASE:
					return call(expression, ".to_lowercase()");
				case CHAR_TO_UPPER_CASE:
					return call(expression, ".to_uppercase()");
				case STRING_ADD_STRING:
					return binary(expression, RustOperator.ADD);
				case STRING_INDEXGET:
					throw new UnsupportedOperationException("Not yet supported!");
				case STRING_RANGEGET: {
					ExpressionString left = expression.target.accept(this);
					Expression argument = expression.arguments.arguments[0];
					if (argument instanceof RangeExpression) {
						RangeExpression rangeArgument = (RangeExpression) argument;
						ExpressionString from = rangeArgument.from.accept(this);
						if ((rangeArgument.to instanceof CallExpression) && ((CallExpression) rangeArgument.to).member.getBuiltin() == BuiltinID.STRING_LENGTH) {
							return new ExpressionString(left.value + "[" + from.value + "..]", RustOperator.INDEX);
						} else {
							ExpressionString to = rangeArgument.to.accept(this);
							return new ExpressionString(left.value + "[" + from.value + ".." + to.value + "]", RustOperator.INDEX);
						}
					} else {
						return new ExpressionString(left.value + "[" + argument.accept(this).value + "]", RustOperator.INDEX);
					}
				}
				case STRING_REMOVE_DIACRITICS:
					throw new UnsupportedOperationException("Not yet supported!");
				case STRING_TRIM:
					return call(expression,"trim");
				case STRING_TO_LOWER_CASE:
					throw new UnsupportedOperationException();
					//return call("toLowerCase", call);
				case STRING_TO_UPPER_CASE:
					throw new UnsupportedOperationException();
					//return call("toUpperCase", call);
				case ASSOC_INDEXGET:
					return call(expression, "get");
				case ASSOC_INDEXSET:
					return call(expression, "put");
				case ASSOC_CONTAINS:
					return call(expression, "containsKey");
				case ASSOC_GETORDEFAULT:
					return call(expression,"get");
				case ASSOC_EQUALS:
					throw new UnsupportedOperationException("Not yet supported!");
				case ASSOC_NOTEQUALS:
					throw new UnsupportedOperationException("Not yet supported!");
				case ASSOC_SAME:
					return binary(expression, RustOperator.EQUALS);
				case ASSOC_NOTSAME:
					return binary(expression, RustOperator.NOTEQUALS);
				case GENERICMAP_GETOPTIONAL:
					throw new UnsupportedOperationException("Not yet supported!");
					//return call("get", call).unaryPrefix(RustOperator.CAST, "(" + scope.type(call.type) + ")");
				case GENERICMAP_PUT:
					throw new UnsupportedOperationException("Not yet supported!");
					//return call("put", call);
				case GENERICMAP_CONTAINS:
					throw new UnsupportedOperationException("Not yet supported!");
					//return call("containsKey", call);
				case GENERICMAP_ADDALL:
					throw new UnsupportedOperationException("Not yet supported!");
					//return call("putAll", call);
				case GENERICMAP_EQUALS:
					throw new UnsupportedOperationException("Not yet supported!");
				case GENERICMAP_NOTEQUALS:
					throw new UnsupportedOperationException("Not yet supported!");
				case GENERICMAP_SAME:
					return binary(expression, RustOperator.EQUALS);
				case GENERICMAP_NOTSAME:
					return binary(expression, RustOperator.NOTEQUALS);
				case ARRAY_INDEXGET:
					return new ExpressionString(expression.target.accept(this) + "[" + expression.arguments.arguments[0].accept(this) + "]", RustOperator.INDEX);
				case ARRAY_INDEXSET: {
					ExpressionString value = expression.arguments.arguments[1].accept(this);
					TypeID baseType = ((ArrayTypeID) (expression.target.type)).elementType;
					String asType = "";
					if (baseType == BasicTypeID.BYTE) {
						asType = "(byte)";
					} else if (baseType == BasicTypeID.USHORT) {
						asType = "(short)";
					}

					if (!asType.isEmpty()) {
						if (value.priority == RustOperator.CAST && value.value.startsWith("(int)"))
							value = new ExpressionString(asType + value.value.substring(5), value.priority);
						else
							value = value.unaryPrefix(RustOperator.CAST, asType);
					}

					return new ExpressionString(
							expression.target.accept(this)
									+ "[" + expression.arguments.arguments[0].accept(this)
									+ "] = "
									+ value.value, RustOperator.INDEX);
				}
				case ARRAY_INDEXGETRANGE: {
					ExpressionString left = expression.target.accept(this);
					Expression argument = expression.arguments.arguments[0];
					if (argument instanceof RangeExpression) {
						RangeExpression rangeArgument = (RangeExpression) argument;
						ExpressionString from = rangeArgument.from.accept(this);
						ExpressionString to = rangeArgument.to.accept(this);
						throw new UnsupportedOperationException("Not yet supported!");
						//return new ExpressionString(scope.type(RustClass.ARRAYS) + ".copyOfRange(" + left.value + ", " + from.value + ", " + to.value + ")", JavaOperator.CALL);
					} else {
						throw new UnsupportedOperationException("Not yet supported!");
					}
				}
				case ARRAY_CONTAINS: {
					throw new UnsupportedOperationException("Not yet supported!");
					//JavaMethod method = scope.fileScope.helperGenerator.createArrayContains((ArrayTypeID) call.target.type);
					//return callAsStatic(scope.fileScope.importer.importType(method.cls) + '.' + method.name, call);
				}
				case ARRAY_EQUALS:
					throw new UnsupportedOperationException("Not yet supported!");
					//return callAsStatic(scope.type(JavaClass.ARRAYS) + ".equals", call);
				case ARRAY_NOTEQUALS:
					throw new UnsupportedOperationException("Not yet supported!");
					//return callAsStatic("!" + scope.type(JavaClass.ARRAYS) + ".equals", call);
				case ARRAY_SAME:
					throw new UnsupportedOperationException("Not yet supported!");
					//return binary(call, JavaOperator.EQUALS);
				case ARRAY_NOTSAME:
					throw new UnsupportedOperationException("Not yet supported!");
					//return binary(call, JavaOperator.NOTEQUALS);
				case FUNCTION_CALL: {
					throw new UnsupportedOperationException("Not yet supported!");
					/*StringBuilder output = new StringBuilder();
					JavaSynthesizedFunctionInstance function = scope.context.getFunction((FunctionTypeID) call.target.type);
					output.append(call.target.accept(this).value);
					output.append(".").append(function.getMethod()).append("(");
					int i = 0;
					for (Expression argument : call.arguments.arguments) {
						if (i > 0)
							output.append(", ");
						output.append(argument.accept(this).value);
						i++;
					}
					output.append(")");
					return new ExpressionString(output.toString(), RustOperator.CALL);*/
				}
				case FUNCTION_SAME:
					return binary(expression, RustOperator.EQUALS);
				case FUNCTION_NOTSAME:
					return binary(expression, RustOperator.NOTEQUALS);
				case OBJECT_SAME:
					return binary(expression, RustOperator.EQUALS);
				case OBJECT_NOTSAME:
					return binary(expression, RustOperator.NOTEQUALS);
				case OPTIONAL_IS_NULL:
					throw new UnsupportedOperationException("Not yet supported!");
					/*return call.target.type.withoutOptional() == BasicTypeID.USIZE
							? call.target.accept(this).unaryPostfix(RustOperator.NOTEQUALS, " < 0")
							: call.target.accept(this).unaryPostfix(RustOperator.EQUALS, " == null");*/
				case OPTIONAL_IS_NOT_NULL:
					throw new UnsupportedOperationException("Not yet supported!");
					/*return call.target.type.withoutOptional() == BasicTypeID.USIZE
							? call.target.accept(this).unaryPostfix(RustOperator.NOTEQUALS, " >= 0")
							: call.target.accept(this).unaryPostfix(RustOperator.NOTEQUALS, " != null");*/
			}

			throw new UnsupportedOperationException("Unknown builtin call: " + expression.member.getBuiltin());
		}

		private ExpressionString unaryPrefix(Expression value, FormattableOperator operator) {
			return value.accept(this).unaryPrefix(operator);
		}

		private ExpressionString unaryPrefix(CallExpression invocation, FormattableOperator operator) {
			return unaryPrefix(invocation.target, operator);
		}

		private ExpressionString unaryPostfix(Expression value, FormattableOperator operator) {
			return value.accept(this).unaryPostfix(operator);
		}

		private ExpressionString unaryPostfix(CallExpression invocation, FormattableOperator operator) {
			return unaryPostfix(invocation.target, operator);
		}

		private ExpressionString call(CallExpression value, String invocation) {
			return new ExpressionString(value.target.accept(this).value + invocation, RustOperator.CALL);
		}

		private ExpressionString binary(CallExpression call, FormattableOperator operator) {
			return binary(call.target, call.arguments.arguments[0], operator);
		}

		private ExpressionString binary(Expression left, Expression right, FormattableOperator operator) {
			return ExpressionString.binary(left.accept(this), right.accept(this), operator);
		}
	}
}
