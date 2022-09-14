package org.openzen.zenscript.formatter;

import org.openzen.zencode.shared.StringExpansion;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.formattershared.ExpressionString;

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
	public ExpressionString visitCompare(CompareExpression expression) {
		return binary(
				expression.left,
				expression.right,
				ZenScriptOperator.getComparison(expression.comparison));
	}

	@Override
	public ExpressionString visitCall(CallExpression expression) {
		MethodID id = expression.method.method.getID();
		switch (id.getKind()) {
			case INSTANCEMETHOD: {
				MethodID.InstanceMethod method = (MethodID.InstanceMethod) id;
				StringBuilder result = new StringBuilder();
				result.append(expression.target.accept(this).value);
				result.append(".");
				result.append(method.name);
				FormattingUtils.formatCall(result, typeFormatter, this, expression.arguments);
				return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
			}
			case OPERATOR:
				return visitOperatorCall(expression, (MethodID.Operator) id);
			case CASTER: {
				StringBuilder result = new StringBuilder();
				if (!expression.method.isImplicit()) {
					result.append(" as ");
					result.append(typeFormatter.format(expression.arguments.typeArguments[0]));
				}
				return new ExpressionString(result.toString(), ZenScriptOperator.CAST);
			}
			case GETTER: {
				MethodID.Getter getter = (MethodID.Getter) id;
				String result = String.valueOf(expression.target.accept(this)) + '.' + getter.name;
				return new ExpressionString(result, ZenScriptOperator.MEMBER);
			}
			case SETTER: {
				MethodID.Setter setter = (MethodID.Setter) id;
				return new ExpressionString(
						expression.target.accept(this) + "." + setter.name + " = " + expression.arguments.arguments[0].accept(this),
						ZenScriptOperator.ASSIGN);
			}
			default: throw new UnsupportedOperationException("Invalid method type: " + id.getKind());
		}
	}

	private ExpressionString visitOperatorCall(CallExpression expression, MethodID.Operator operator) {
		switch (operator.operator) {
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
				if (!expression.method.isImplicit()) {
					result.append(" as ");
					result.append(typeFormatter.format(expression.arguments.typeArguments[0]));
				}
				return new ExpressionString(result.toString(), ZenScriptOperator.CAST);
			}
			default:
				throw new UnsupportedOperationException("Unknown operator: " + operator);
		}
	}

	@Override
	public ExpressionString visitCallStatic(CallStaticExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.target.accept(typeFormatter));

		MethodID id = expression.member.getID();
		switch (id.getKind()) {
			case INSTANCEMETHOD: {
				MethodID.InstanceMethod method = (MethodID.InstanceMethod) id;
				result.append(".");
				result.append(method.name);
				FormattingUtils.formatCall(result, typeFormatter, this, expression.arguments);
				return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
			}
			case OPERATOR:
				MethodID operator = (MethodID.Operator) id;
				if (operator == OperatorType.CONSTRUCTOR) {
					StringBuilder result = new StringBuilder();
					result.append("new ");
					result.append(typeFormatter.format(expression.type));
					FormattingUtils.formatCall(result, typeFormatter, this, expression.arguments);
					return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
				} else if (operator.operator == OperatorType.CALL) {
					// nothing
				} else {
					result.append(".");
					result.append(operator.operator);
				}
				FormattingUtils.formatCall(result, typeFormatter, this, expression.arguments);
				return new ExpressionString(result.toString(), ZenScriptOperator.PRIMARY);
			case GETTER: {
				MethodID.Getter getter = (MethodID.Getter) id;
				result.append(".").append(getter.name);
				return new ExpressionString(result.toString(), ZenScriptOperator.MEMBER);
			}
			case SETTER: {
				MethodID.Setter setter = (MethodID.Setter) id;
				result.append(".").append(setter.name).append(" = ").append(expression.arguments.arguments[0].accept(this));
				return new ExpressionString(result.toString(), ZenScriptOperator.ASSIGN);
			}
			default: throw new UnsupportedOperationException("Invalid method type: " + id.getKind());
		}
	}

	@Override
	public ExpressionString visitCapturedClosure(CapturedClosureExpression expression) {
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
		String result = String.valueOf(expression.target.accept(this)) + '.' + expression.field.getName();
		return new ExpressionString(result, ZenScriptOperator.MEMBER);
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
		MethodID.Operator operator = (MethodID.Operator) expression.member.getID();
		return unaryPostfix(expression.target, ZenScriptOperator.INCREMENT, operator.operator == OperatorType.INCREMENT ? "++" : "--");
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
				expression.target.accept(this) + "." + expression.field.getName() + " = " + expression.value.accept(this).value,
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
				typeFormatter.format(expression.type) + "." + expression.field.getName() + " = " + expression.value.accept(this).value,
				ZenScriptOperator.ASSIGN);
	}

	@Override
	public ExpressionString visitSupertypeCast(SupertypeCastExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public ExpressionString visitSubtypeCast(SubtypeCastExpression expression) {
		String result = expression.value.accept(this).value + " as " + typeFormatter.format(expression.type);
		return new ExpressionString(result, ZenScriptOperator.CAST);
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
