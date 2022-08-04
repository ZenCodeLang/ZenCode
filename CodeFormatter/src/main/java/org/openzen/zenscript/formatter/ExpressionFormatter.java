package org.openzen.zenscript.formatter;

import org.openzen.zencode.shared.StringExpansion;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.formattershared.ExpressionString;

import java.util.Optional;

public class ExpressionFormatter implements ExpressionVisitor<ExpressionString> {
	public final TypeFormatter typeFormatter;
	public final String indent;
	private final ScriptFormattingSettings settings;

	public ExpressionFormatter(ScriptFormattingSettings settings, TypeFormatter typeFormatter, String indent) {
		this.settings = settings;
		this.typeFormatter = typeFormatter;
		this.indent = indent;
	}

	@Override
	public ExpressionString visitAndAnd(AndAndExpression expression) {
		return binary(expression.left, expression.right, ZenScriptOperator.ANDAND);
	}

	@Override
	public ExpressionString visitArray(ArrayExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append("[");
		int index = 0;
		for (Expression element : expression.expressions) {
			if (index > 0)
				result.append(", ");

			result.append(element.accept(this).value);
			index++;
		}
		result.append("]");
		return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitBinary(BinaryExpression expression) {
		switch (expression.operator) {
			default:
				throw new UnsupportedOperationException("Unknown operator: " + expression.operator);
		}
	}

	@Override
	public ExpressionString visitCompare(CompareExpression expression) {
		return binary(
				expression.left,
				expression.right,
				ZenScriptOperator.getComparison(expression.comparison));
	}

	@Override
	public ExpressionString visitCall(CallExpression expression) {
		return expression.method.method.getOperator().map(operator -> {
			switch (operator) {
				case NOT:
					return unaryPrefix(expression.target, ZenScriptOperator.NOT, "!");
				case NEG:
					return unaryPrefix(expression.target, ZenScriptOperator.NEG, "-");
				case CAT:
					if (expression.arguments.arguments.length == 0) {
						return unaryPrefix(expression.target, ZenScriptOperator.INVERT, "~");
					} else {
						return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.CAT);
					}
				case ADD:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.ADD);
				case SUB:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.SUB);
				case MUL:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.MUL);
				case DIV:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.DIV);
				case MOD:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.MOD);
				case AND:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.AND);
				case OR:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.OR);
				case XOR:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.XOR);
				case CONTAINS:
					return binary(expression.getFirstArgument(), expression.target, ZenScriptOperator.CONTAINS);
				case EQUALS:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.EQUALS);
				case NOTEQUALS:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.NOTEQUALS);
				case INDEXSET: {
					StringBuilder result = new StringBuilder();
					result.append(expression.target);
					result.append("[");
					for (int i = 0; i < expression.arguments.arguments.length - 1; i++) {
						if (i > 0)
							result.append(", ");

						result.append(expression.arguments.arguments[i].accept(this).value);
					}
					result.append("] = ");
					result.append(expression.arguments.arguments[expression.arguments.arguments.length - 1].accept(this).value);
					return new ExpressionString(result.toString(), ZenScriptOperator.ASSIGN);
				}
				case INDEXGET: {
					StringBuilder result = new StringBuilder();
					if (expression.target instanceof GetLocalVariableExpression) {
						result.append(((GetLocalVariableExpression) expression.target).variable.name);
					} else if (expression.target instanceof GetFunctionParameterExpression) {
						result.append(((GetFunctionParameterExpression) expression.target).parameter.name);
					} else {
						result.append(expression.target);
					}
					result.append("[");
					//why -1?
					for (int i = 0; i < expression.arguments.arguments.length; i++) {
						if (i > 0)
							result.append(", ");

						result.append(expression.arguments.arguments[i].accept(this));
					}
					result.append("]");
					return new ExpressionString(result.toString(), ZenScriptOperator.INDEX);
				}
				case MEMBERGETTER: {
					StringBuilder result = new StringBuilder();
					result.append(expression.target);
					result.append(".get(");
					result.append(expression.getFirstArgument().accept(this));
					result.append(")");
					return new ExpressionString(result.toString(), ZenScriptOperator.MEMBER);
				}
				case MEMBERSETTER: {
					StringBuilder result = new StringBuilder();
					result.append(expression.target);
					result.append(".set(");
					result.append(expression.getFirstArgument().accept(this));
					result.append(", ");
					result.append(expression.arguments.arguments[1].accept(this));
					result.append(")");
					return new ExpressionString(result.toString(), ZenScriptOperator.MEMBER);
				}
				case ADDASSIGN:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.ADDASSIGN);
				case SUBASSIGN:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.SUBASSIGN);
				case MULASSIGN:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.MULASSIGN);
				case DIVASSIGN:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.DIVASSIGN);
				case MODASSIGN:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.MODASSIGN);
				case CATASSIGN:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.CATASSIGN);
				case ORASSIGN:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.ORASSIGN);
				case ANDASSIGN:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.ANDASSIGN);
				case XORASSIGN:
					return binary(expression.target, expression.getFirstArgument(), ZenScriptOperator.XORASSIGN);
				case INCREMENT:
					return unaryPrefix(expression.target, ZenScriptOperator.DECREMENT, "++");
				case DECREMENT:
					return unaryPrefix(expression.target, ZenScriptOperator.DECREMENT, "--");
				case CALL: {
					StringBuilder result = new StringBuilder();
					FormattingUtils.formatCall(result, typeFormatter, this, expression.arguments);
					return new ExpressionString(result.toString(), ZenScriptOperator.CALL);
				}
				case CAST: {
					StringBuilder result = new StringBuilder();
					result.append(" as ");
					result.append(typeFormatter.format(expression.arguments.typeArguments[0]));
					return new ExpressionString(result.toString(), ZenScriptOperator.CAST);
				}
				default:
					throw new UnsupportedOperationException("Unknown operator: " + operator);
			}
		}).orElseGet(() -> {
			StringBuilder result = new StringBuilder();
			result.append(expression.target.accept(this).value);
			result.append(".");
			result.append(expression.method.getName());
			FormattingUtils.formatCall(result, typeFormatter, this, expression.arguments);
			return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
		});
	}

	@Override
	public ExpressionString visitCallStatic(CallStaticExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.target.accept(typeFormatter));
		Optional<OperatorType> operator = expression.member.method.getOperator();
		if (operator.isPresent()) {
			if (operator.get() == OperatorType.CALL) {
				// nothing
			} else {
				result.append(".");
				result.append(operator.get().operator);
			}
		} else {
			result.append(".");
			result.append(expression.member.getName());
		}
		FormattingUtils.formatCall(result, typeFormatter, this, expression.arguments);
		return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitCapturedClosure(CapturedClosureExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ExpressionString visitCapturedDirect(CapturedDirectExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ExpressionString visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
		return new ExpressionString(expression.variable.name, ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitCapturedParameter(CapturedParameterExpression expression) {
		return new ExpressionString(expression.parameter.name, ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitCapturedThis(CapturedThisExpression expression) {
		return new ExpressionString("this", ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitCast(CastExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.target.accept(this).value);
		if (!expression.isImplicit) {
			result.append(" as ");
			result.append(typeFormatter.format(expression.type));
		}
		return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitCheckNull(CheckNullExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public ExpressionString visitCoalesce(CoalesceExpression expression) {
		return binary(expression.left, expression.right, ZenScriptOperator.COALESCE);
	}

	@Override
	public ExpressionString visitConditional(ConditionalExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.condition.accept(this));
		result.append(" ? ");
		result.append(expression.ifThen.accept(this));
		result.append(" : ");
		result.append(expression.ifElse.accept(this));
		return new ExpressionString(result.toString(), ZenScriptOperator.TERNARY);
	}

	@Override
	public ExpressionString visitConst(ConstExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(typeFormatter.format(expression.type));
		result.append('.');
		result.append(expression.constant.getName());
		return new ExpressionString(result.toString(), ZenScriptOperator.MEMBER);
	}

	@Override
	public ExpressionString visitConstantBool(ConstantBoolExpression expression) {
		return new ExpressionString(expression.value ? "true" : "false", ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantByte(ConstantByteExpression expression) {
		return new ExpressionString(Integer.toString(expression.value) + " as byte", ZenScriptOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantChar(ConstantCharExpression expression) {
		return new ExpressionString(
				StringExpansion.escape(Character.toString(expression.value), '\'', true),
				ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantDouble(ConstantDoubleExpression expression) {
		return new ExpressionString(Double.toString(expression.value), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantFloat(ConstantFloatExpression expression) {
		return new ExpressionString(Float.toString(expression.value), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantInt(ConstantIntExpression expression) {
		return new ExpressionString(Integer.toString(expression.value), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantLong(ConstantLongExpression expression) {
		return new ExpressionString(Long.toString(expression.value), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantSByte(ConstantSByteExpression expression) {
		return new ExpressionString(Byte.toString(expression.value) + " as sbyte", ZenScriptOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantShort(ConstantShortExpression expression) {
		return new ExpressionString(Integer.toString(expression.value) + " as short", ZenScriptOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantUSize(ConstantUSizeExpression expression) {
		return new ExpressionString(Long.toUnsignedString(expression.value) + " as usize", ZenScriptOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantString(ConstantStringExpression expression) {
		return new ExpressionString(StringExpansion.escape(
				expression.value,
				settings.useSingleQuotesForStrings ? '\'' : '"',
				true), ZenScriptOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantUInt(ConstantUIntExpression expression) {
		return new ExpressionString(Integer.toUnsignedString(expression.value) + " as uint", ZenScriptOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantULong(ConstantULongExpression expression) {
		return new ExpressionString(Long.toUnsignedString(expression.value) + " as ulong", ZenScriptOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantUShort(ConstantUShortExpression expression) {
		return new ExpressionString(Integer.toUnsignedString(expression.value) + " as ushort", ZenScriptOperator.CAST);
	}

	@Override
	public ExpressionString visitConstructorThisCall(ConstructorThisCallExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append("this");
		FormattingUtils.formatCall(result, typeFormatter, this, expression.arguments);
		return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append("super");
		FormattingUtils.formatCall(result, typeFormatter, this, expression.arguments);
		return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitEnumConstant(EnumConstantExpression expression) {
		return new ExpressionString(typeFormatter.format(expression.type) + "." + expression.value.name, ZenScriptOperator.MEMBER);
	}

	@Override
	public ExpressionString visitFunction(FunctionExpression expression) {
		StringBuilder result = new StringBuilder();
		if (expression.header.parameters.length == 1) {
			result.append(expression.header.parameters[0].name);
		} else {
			result.append('(');
			for (int i = 0; i < expression.header.parameters.length; i++) {
				if (i > 0)
					result.append(", ");
				result.append(expression.header.parameters[i].name);
			}
			result.append(')');
		}
		result.append(" => ");

		if (expression.body instanceof ReturnStatement) {
			result.append(((ReturnStatement) expression.body).value.accept(this));
		} else if (expression.body instanceof ExpressionStatement) {
			result.append(((ExpressionStatement) expression.body).expression.accept(this));
		} else {
			StatementFormatter formatter = new StatementFormatter(result, indent + settings.indent + settings.indent, settings, this);
			expression.body.accept(formatter);
		}
		return new ExpressionString(result.toString(), ZenScriptOperator.FUNCTION);
	}

	@Override
	public ExpressionString visitGetField(GetFieldExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.target.accept(this));
		result.append('.');
		result.append(expression.field.member.name);
		return new ExpressionString(result.toString(), ZenScriptOperator.MEMBER);
	}

	@Override
	public ExpressionString visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		return new ExpressionString(expression.parameter.name, ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitGetLocalVariable(GetLocalVariableExpression expression) {
		return new ExpressionString(expression.variable.name, ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitGetMatchingVariantField(GetMatchingVariantField expression) {
		return new ExpressionString(expression.value.parameters[expression.index], ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitGetStaticField(GetStaticFieldExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(typeFormatter.format(expression.type));
		result.append('.');
		result.append(expression.field.member.name);
		return new ExpressionString(result.toString(), ZenScriptOperator.MEMBER);
	}

	@Override
	public ExpressionString visitGetter(GetterExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.target.accept(this));
		result.append('.');
		result.append(expression.getter.getName());
		return new ExpressionString(result.toString(), ZenScriptOperator.MEMBER);
	}

	@Override
	public ExpressionString visitGlobal(GlobalExpression expression) {
		return new ExpressionString(expression.name, ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitGlobalCall(GlobalCallExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.name);
		FormattingUtils.formatCall(result, typeFormatter, this, expression.arguments);
		return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitInterfaceCast(InterfaceCastExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.value.accept(this).value);
		result.append(" as ");
		result.append(typeFormatter.format(expression.type));
		return new ExpressionString(result.toString(), ZenScriptOperator.CAST);
	}

	@Override
	public ExpressionString visitIs(IsExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.value.accept(this).value);
		result.append(" is ");
		result.append(typeFormatter.format(expression.isType));
		return new ExpressionString(result.toString(), ZenScriptOperator.IS);
	}

	@Override
	public ExpressionString visitMakeConst(MakeConstExpression expression) {
		return expression.accept(this);
	}

	@Override
	public ExpressionString visitMap(MapExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append("{");
		for (int i = 0; i < expression.keys.length; i++) {
			if (i > 0)
				result.append(", ");
			result.append(expression.keys[i].accept(this));
			result.append(": ");
			result.append(expression.values[i].accept(this));
		}
		result.append("}");
		return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitMatch(MatchExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append("match ");
		result.append(expression.value.accept(this));
		result.append(" {\n");

		return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitNew(NewExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append("new ");
		result.append(typeFormatter.format(expression.type));
		FormattingUtils.formatCall(result, typeFormatter, this, expression.arguments);
		return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitNull(NullExpression expression) {
		return new ExpressionString("null", ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitOrOr(OrOrExpression expression) {
		return binary(expression.left, expression.right, ZenScriptOperator.OROR);
	}

	@Override
	public ExpressionString visitPanic(PanicExpression expression) {
		return expression.value.accept(this).unaryPrefix(ZenScriptOperator.PANIC);
	}

	@Override
	public ExpressionString visitPlatformSpecific(Expression expression) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public ExpressionString visitPostCall(PostCallExpression expression) {
		return unaryPostfix(expression.target, ZenScriptOperator.INCREMENT, expression.member.getOperator() == OperatorType.INCREMENT ? "++" : "--");
	}

	@Override
	public ExpressionString visitRange(RangeExpression expression) {
		return binary(expression.from, expression.to, ZenScriptOperator.RANGE);
	}

	@Override
	public ExpressionString visitSameObject(SameObjectExpression expression) {
		return binary(expression.left, expression.right, expression.inverted ? ZenScriptOperator.NOTSAME : ZenScriptOperator.SAME);
	}

	@Override
	public ExpressionString visitSetField(SetFieldExpression expression) {
		return new ExpressionString(
				expression.target.accept(this) + "." + expression.field.member.name + " = " + expression.value.accept(this).value,
				ZenScriptOperator.ASSIGN);
	}

	@Override
	public ExpressionString visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		return new ExpressionString(
				expression.parameter.name + " = " + expression.value.accept(this).value,
				ZenScriptOperator.ASSIGN);
	}

	@Override
	public ExpressionString visitSetLocalVariable(SetLocalVariableExpression expression) {
		return new ExpressionString(
				expression.variable.name + " = " + expression.value.accept(this).value,
				ZenScriptOperator.ASSIGN);
	}

	@Override
	public ExpressionString visitSetStaticField(SetStaticFieldExpression expression) {
		return new ExpressionString(
				typeFormatter.format(expression.type) + "." + expression.field.member.name + " = " + expression.value.accept(this).value,
				ZenScriptOperator.ASSIGN);
	}

	@Override
	public ExpressionString visitSetter(SetterExpression expression) {
		return new ExpressionString(
				expression.target.accept(this) + "." + expression.setter.member.name + " = " + expression.value.accept(this),
				ZenScriptOperator.ASSIGN);
	}

	@Override
	public ExpressionString visitStaticGetter(StaticGetterExpression expression) {
		return new ExpressionString(
				typeFormatter.format(expression.type) + "." + expression.getter.member.name,
				ZenScriptOperator.MEMBER);
	}

	@Override
	public ExpressionString visitStaticSetter(StaticSetterExpression expression) {
		return new ExpressionString(
				typeFormatter.format(expression.type) + "." + expression.setter.member.name + " = " + expression.setter.member.name,
				ZenScriptOperator.ASSIGN);
	}

	@Override
	public ExpressionString visitSupertypeCast(SupertypeCastExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public ExpressionString visitSubtypeCast(SubtypeCastExpression expression) {

		StringBuilder result = new StringBuilder(expression.value.accept(this).value);
		result.append(" as ");
		result.append(typeFormatter.format(expression.type));
		return new ExpressionString(result.toString(), ZenScriptOperator.CAST);
	}

	@Override
	public ExpressionString visitThis(ThisExpression expression) {
		return new ExpressionString("this", ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitThrow(ThrowExpression expression) {
		return new ExpressionString("throw " + expression.value.accept(this), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitTryConvert(TryConvertExpression expression) {
		ExpressionString value = expression.value.accept(this);
		return new ExpressionString("try?" + value.value, value.priority);
	}

	@Override
	public ExpressionString visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
		ExpressionString value = expression.value.accept(this);
		return new ExpressionString("try!" + value.value, value.priority);
	}

	@Override
	public ExpressionString visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
		ExpressionString value = expression.value.accept(this);
		return new ExpressionString("try!" + value.value, value.priority);
	}

	@Override
	public ExpressionString visitUnary(UnaryExpression expression) {
		switch (expression.operator.group) {
			case BOOLEAN_NOT:
				return unaryPrefix(expression.target, ZenScriptOperator.NOT, "!");
			case BITWISE_NOT:
				return unaryPrefix(expression.target, ZenScriptOperator.NOT, "~");
			case NEG:
				return unaryPrefix(expression.target, ZenScriptOperator.NEG, "-");
			case CAST_IMPLICIT:
			case CAST_EXPLICIT:
				// TODO: skip implicit casts where possible
				return unaryPostfix(expression.target, ZenScriptOperator.CAST, " as " + typeFormatter.format(expression.type));
			case COUNT_LOW_ZEROES:
				return callUnary(expression.target, "countLowZeroes");
			case COUNT_HIGH_ZEROES:
				return callUnary(expression.target, "countHighZeroes");
			case COUNT_LOW_ONES:
				return callUnary(expression.target, "countLowOnes");
			case COUNT_HIGH_ONES:
				return callUnary(expression.target, "countHighOnes");
			case HIGHEST_ONE_BIT:
				return callUnary(expression.target, "highestOneBit");
			case LOWEST_ONE_BIT:
				return callUnary(expression.target, "lowestOneBit");
			case HIGHEST_ZERO_BIT:
				return callUnary(expression.target, "highestZeroBit");
			case LOWEST_ZERO_BIT:
				return callUnary(expression.target, "lowestZeroBit");
			case BIT_COUNT:
				return callUnary(expression.target, "bitCount");
			case PARSE:
			case OTHER:
				switch (expression.operator) {
					case BOOL_PARSE:
						return callStaticUnary("bool.parse", expression.target);
					case BYTE_PARSE:
						return callStaticUnary("byte.parse", expression.target);
					case SBYTE_PARSE:
						return callStaticUnary("sbyte.parse", expression.target);
					case SHORT_PARSE:
						return callStaticUnary("short.parse", expression.target);
					case USHORT_PARSE:
						return callStaticUnary("ushort.parse", expression.target);
					case INT_PARSE:
						return callStaticUnary("int.parse", expression.target);
					case UINT_PARSE:
						return callStaticUnary("uint.parse", expression.target);
					case LONG_PARSE:
						return callStaticUnary("long.parse", expression.target);
					case ULONG_PARSE:
						return callStaticUnary("ulong.parse", expression.target);
					case USIZE_PARSE:
						return callStaticUnary("usize.parse", expression.target);
					case FLOAT_PARSE:
						return callStaticUnary("float.parse", expression.target);
					case DOUBLE_PARSE:
						return callStaticUnary("double.parse", expression.target);
					case FLOAT_BITS:
					case DOUBLE_BITS:
						return unaryPostfix(expression, ZenScriptOperator.MEMBER, ".bits");
					case FLOAT_FROM_BITS:
						return callStaticUnary("float.fromBits", expression.target);
					case DOUBLE_FROM_BITS:
						return callStaticUnary("double.fromBits", expression.target);
					case CHAR_TO_UNICODE:
						return callUnary(expression.target, "toUnicode");
					case CHAR_FROM_UNICODE:
						return callStaticUnary("char.fromUnicode", expression.target);
					case CHAR_REMOVE_DIACRITICS:
						return unaryPostfix(expression.target, ZenScriptOperator.CALL, "removeDiacritics");
					case CHAR_TO_LOWER_CASE:
						return unaryPostfix(expression.target, ZenScriptOperator.CALL, "toLowerCase");
					case CHAR_TO_UPPER_CASE:
						return unaryPostfix(expression.target, ZenScriptOperator.CAST, "toUpperCase");
					case STRING_CONSTRUCTOR_CHARACTERS:
						return callStaticUnary("new string", expression.target);
					case STRING_LENGTH:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".length");
					case STRING_CHARACTERS:
						return unaryPostfix(expression.target, ZenScriptOperator.CALL, ".characters()");
					case STRING_ISEMPTY:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".empty");
					case STRING_REMOVE_DIACRITICS:
						return callUnary(expression.target, "removeDiacritics");
					case STRING_TRIM:
						return callUnary(expression.target, "trim");
					case STRING_TO_LOWER_CASE:
						return callUnary(expression.target, "toLowerCase");
					case STRING_TO_UPPER_CASE:
						return callUnary(expression.target, "toUpperCase");
					case ASSOC_SIZE:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".size");
					case ASSOC_ISEMPTY:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".empty");
					case ASSOC_KEYS:
						return callUnary(expression.target, "keys");
					case ASSOC_VALUES:
						return callUnary(expression.target, "values");
					case GENERICMAP_SIZE:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".size");
					case GENERICMAP_ISEMPTY:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".empty");
					case ARRAY_LENGTH:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".length");
					case ARRAY_ISEMPTY:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".empty");
					case ENUM_NAME:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".name");
					case ENUM_ORDINAL:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".ordinal");
					case OPTIONAL_WRAP:
						return expression.target.accept(this);
					case OPTIONAL_UNWRAP:
						return unaryPostfix(expression.target, ZenScriptOperator.NOT, "!");
					case RANGE_FROM:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".from");
					case RANGE_TO:
						return unaryPostfix(expression.target, ZenScriptOperator.MEMBER, ".to");
				}
			default:
				throw new UnsupportedOperationException("Unknown operator: " + expression.operator);
		}
	}

	private ExpressionString callUnary(Expression target, String method) {
		return unaryPostfix(target, ZenScriptOperator.CAST, "." + method + "()");
	}

	private ExpressionString callStaticUnary(String method, Expression target) {
		return new ExpressionString(method + "(" + target.accept(this) + ")", ZenScriptOperator.CALL);
	}

	@Override
	public ExpressionString visitVariantValue(VariantValueExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append("new ");
		// TODO: finish this
		return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitWrapOptional(WrapOptionalExpression expression) {
		return expression.value.accept(this);
	}

	private ExpressionString unaryPrefix(Expression value, ZenScriptOperator operator, String operatorString) {
		return new ExpressionString(operatorString + value.accept(this).value, operator);
	}

	private ExpressionString unaryPostfix(Expression value, ZenScriptOperator operator, String operatorString) {
		return new ExpressionString(value.accept(this).value + operatorString, operator);
	}

	private ExpressionString binary(Expression left, Expression right, ZenScriptOperator operator) {
		return ExpressionString.binary(left.accept(this), right.accept(this), operator);
	}
}
