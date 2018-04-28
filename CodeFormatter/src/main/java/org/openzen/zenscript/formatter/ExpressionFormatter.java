/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

import org.openzen.zenscript.codemodel.expression.AndAndExpression;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.BasicCompareExpression;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallExpression;
import org.openzen.zenscript.codemodel.expression.CallStaticExpression;
import org.openzen.zenscript.codemodel.expression.CapturedClosureExpression;
import org.openzen.zenscript.codemodel.expression.CapturedDirectExpression;
import org.openzen.zenscript.codemodel.expression.CapturedLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.CapturedParameterExpression;
import org.openzen.zenscript.codemodel.expression.CapturedThisExpression;
import org.openzen.zenscript.codemodel.expression.CastExpression;
import org.openzen.zenscript.codemodel.expression.CheckNullExpression;
import org.openzen.zenscript.codemodel.expression.CoalesceExpression;
import org.openzen.zenscript.codemodel.expression.ConditionalExpression;
import org.openzen.zenscript.codemodel.expression.ConstantBoolExpression;
import org.openzen.zenscript.codemodel.expression.ConstantByteExpression;
import org.openzen.zenscript.codemodel.expression.ConstantCharExpression;
import org.openzen.zenscript.codemodel.expression.ConstantDoubleExpression;
import org.openzen.zenscript.codemodel.expression.ConstantFloatExpression;
import org.openzen.zenscript.codemodel.expression.ConstantIntExpression;
import org.openzen.zenscript.codemodel.expression.ConstantLongExpression;
import org.openzen.zenscript.codemodel.expression.ConstantSByteExpression;
import org.openzen.zenscript.codemodel.expression.ConstantShortExpression;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUIntExpression;
import org.openzen.zenscript.codemodel.expression.ConstantULongExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUShortExpression;
import org.openzen.zenscript.codemodel.expression.ConstructorSuperCallExpression;
import org.openzen.zenscript.codemodel.expression.ConstructorThisCallExpression;
import org.openzen.zenscript.codemodel.expression.EnumConstantExpression;
import org.openzen.zenscript.codemodel.expression.EqualsExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.GenericCompareExpression;
import org.openzen.zenscript.codemodel.expression.GetFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.GetStaticFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetterExpression;
import org.openzen.zenscript.codemodel.expression.InterfaceCastExpression;
import org.openzen.zenscript.codemodel.expression.IsExpression;
import org.openzen.zenscript.codemodel.expression.MakeConstExpression;
import org.openzen.zenscript.codemodel.expression.MapExpression;
import org.openzen.zenscript.codemodel.expression.NewExpression;
import org.openzen.zenscript.codemodel.expression.NotExpression;
import org.openzen.zenscript.codemodel.expression.NullExpression;
import org.openzen.zenscript.codemodel.expression.OrOrExpression;
import org.openzen.zenscript.codemodel.expression.RangeExpression;
import org.openzen.zenscript.codemodel.expression.SetFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.SetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.SetStaticFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticSetterExpression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.expression.WrapOptionalExpression;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.StringUtils;

/**
 *
 * @author Hoofdgebruiker
 */
public class ExpressionFormatter implements ExpressionVisitor<ExpressionString> {
	private final FormattingSettings settings;
	public final TypeFormatter typeFormatter;
	
	public ExpressionFormatter(FormattingSettings settings, TypeFormatter typeFormatter) {
		this.settings = settings;
		this.typeFormatter = typeFormatter;
	}

	@Override
	public ExpressionString visitAndAnd(AndAndExpression expression) {
		return binary(expression.left, expression.right, OperatorPriority.ANDAND, " && ");
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
		return new ExpressionString(result.toString(), OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitCompare(BasicCompareExpression expression) {
		return binary(
				expression.left,
				expression.right,
				OperatorPriority.COMPARE,
				" " + expression.operator.str + " ");
	}

	@Override
	public ExpressionString visitCall(CallExpression expression) {
		if (expression.member instanceof OperatorMember) {
			OperatorMember operator = (OperatorMember) expression.member;
			switch (operator.operator) {
				case NOT:
					return unaryPrefix(expression.target, OperatorPriority.NOT, "!");
				case NEG:
					return unaryPrefix(expression.target, OperatorPriority.NEG, "-");
				case CAT:
					if (expression.arguments.arguments.length == 0) {
						return unaryPrefix(expression.target, OperatorPriority.INVERT, "~");
					} else {
						return binary(expression.target, expression.getFirstArgument(), OperatorPriority.CAT, " ~ ");
					}
				case ADD:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.ADD, " + ");
				case SUB:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.SUB, " - ");
				case MUL:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.MUL, " * ");
				case DIV:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.DIV, " / ");
				case MOD:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.MOD, " % ");
				case AND:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.AND, " & ");
				case OR:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.OR, " | ");
				case XOR:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.XOR, " ^ ");
				case CONTAINS:
					return binary(expression.getFirstArgument(), expression.target, OperatorPriority.CONTAINS, " in ");
				case EQUALS:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.COMPARE, " == ");
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
					return new ExpressionString(result.toString(), OperatorPriority.ASSIGN);
				}
				case INDEXGET: {
					StringBuilder result = new StringBuilder();
					result.append(expression.target);
					result.append("[");
					for (int i = 0; i < expression.arguments.arguments.length - 1; i++) {
						if (i > 0)
							result.append(", ");
						
						result.append(expression.arguments.arguments[i].accept(this));
					}
					result.append("]");
					return new ExpressionString(result.toString(), OperatorPriority.INDEX);
				}
				case MEMBERGETTER: {
					StringBuilder result = new StringBuilder();
					result.append(expression.target);
					result.append(".get(");
					result.append(expression.getFirstArgument().accept(this));
					result.append(")");
					return new ExpressionString(result.toString(), OperatorPriority.MEMBER);
				}
				case MEMBERSETTER: {
					StringBuilder result = new StringBuilder();
					result.append(expression.target);
					result.append(".set(");
					result.append(expression.getFirstArgument().accept(this));
					result.append(", ");
					result.append(expression.arguments.arguments[1].accept(this));
					result.append(")");
					return new ExpressionString(result.toString(), OperatorPriority.MEMBER);
				}
				case ADDASSIGN:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.XORASSIGN, " += ");
				case SUBASSIGN:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.XORASSIGN, " -= ");
				case MULASSIGN:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.XORASSIGN, " *= ");
				case DIVASSIGN:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.XORASSIGN, " /= ");
				case MODASSIGN:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.XORASSIGN, " %= ");
				case CATASSIGN:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.XORASSIGN, " ~= ");
				case ORASSIGN:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.XORASSIGN, " |= ");
				case ANDASSIGN:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.XORASSIGN, " &= ");
				case XORASSIGN:
					return binary(expression.target, expression.getFirstArgument(), OperatorPriority.XORASSIGN, " ^= ");
				case POST_INCREMENT:
					return unaryPostfix(expression.target, OperatorPriority.DECREMENT, "++");
				case POST_DECREMENT:
					return unaryPostfix(expression.target, OperatorPriority.DECREMENT, "--");
				case PRE_INCREMENT:
					return unaryPrefix(expression.target, OperatorPriority.DECREMENT, "++");
				case PRE_DECREMENT:
					return unaryPrefix(expression.target, OperatorPriority.DECREMENT, "--");
				case CALL: {
					StringBuilder result = new StringBuilder();
					result.append(".");
					format(result, expression.arguments);
					return new ExpressionString(result.toString(), OperatorPriority.CALL);
				}
				case CAST: {
					StringBuilder result = new StringBuilder();
					result.append(" as ");
					result.append(expression.arguments.typeArguments[0].accept(typeFormatter));
					return new ExpressionString(result.toString(), OperatorPriority.CAST);
				}
				default:
					throw new UnsupportedOperationException("Unknown operator: " + operator.operator);
			}
		} else {
			StringBuilder result = new StringBuilder();
			result.append(expression.target.accept(this).value);
			result.append(".");
			result.append(expression.member.name);
			format(result, expression.arguments);
			return new ExpressionString(result.toString(), OperatorPriority.PRIMARY);
		}
	}

	@Override
	public ExpressionString visitCallStatic(CallStaticExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.type.accept(typeFormatter));
		result.append(".");
		result.append(expression.member.name);
		format(result, expression.arguments);
		return new ExpressionString(result.toString(), OperatorPriority.PRIMARY);
	}
	
	private void format(StringBuilder result, CallArguments arguments) {
		if (arguments == null || arguments.typeArguments == null)
			throw new IllegalArgumentException("Arguments cannot be null!");
		
		if (arguments.typeArguments.length > 0) {
			result.append("<");
			
			int index = 0;
			for (ITypeID typeArgument : arguments.typeArguments) {
				if (index > 0)
					result.append(", ");
				result.append(typeArgument.accept(typeFormatter));
				index++;
			}
			result.append(">");
		}
		result.append("(");
		int index = 0;
		for (Expression argument : arguments.arguments) {
			if (index > 0)
				result.append(", ");
			result.append(argument.accept(this).value);
			index++;
		}
		result.append(")");
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
		return new ExpressionString(expression.variable.name, OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitCapturedParameter(CapturedParameterExpression expression) {
		return new ExpressionString(expression.parameter.name, OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitCapturedThis(CapturedThisExpression expression) {
		return new ExpressionString("this", OperatorPriority.PRIMARY);
	}
	
	@Override
	public ExpressionString visitCast(CastExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.target.accept(this).value);
		if (!expression.isImplicit) {
			result.append(" as ");
			result.append(expression.member.toType.accept(typeFormatter));
		}
		return new ExpressionString(result.toString(), OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitCheckNull(CheckNullExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public ExpressionString visitCoalesce(CoalesceExpression expression) {
		return binary(expression.left, expression.right, OperatorPriority.COALESCE, " ?? ");
	}

	@Override
	public ExpressionString visitConditional(ConditionalExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.condition.accept(this));
		result.append(" ? ");
		result.append(expression.ifThen.accept(this));
		result.append(" : ");
		result.append(expression.ifElse.accept(this));
		return new ExpressionString(result.toString(), OperatorPriority.TERNARY);
	}

	@Override
	public ExpressionString visitConstantBool(ConstantBoolExpression expression) {
		return new ExpressionString(expression.value ? "true" : "false", OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantByte(ConstantByteExpression expression) {
		return new ExpressionString(Byte.toString(expression.value) + " as byte", OperatorPriority.CAST);
	}

	@Override
	public ExpressionString visitConstantChar(ConstantCharExpression expression) {
		return new ExpressionString(
				StringUtils.escape(Character.toString(expression.value), '\'', true),
				OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantDouble(ConstantDoubleExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ExpressionString visitConstantFloat(ConstantFloatExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ExpressionString visitConstantInt(ConstantIntExpression expression) {
		return new ExpressionString(Integer.toString(expression.value), OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantLong(ConstantLongExpression expression) {
		return new ExpressionString(Long.toString(expression.value), OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantSByte(ConstantSByteExpression expression) {
		return new ExpressionString(Byte.toString(expression.value) + " as sbyte", OperatorPriority.CAST);
	}

	@Override
	public ExpressionString visitConstantShort(ConstantShortExpression expression) {
		return new ExpressionString(Integer.toString(expression.value) + " as short", OperatorPriority.CAST);
	}

	@Override
	public ExpressionString visitConstantString(ConstantStringExpression expression) {
		return new ExpressionString(StringUtils.escape(
				expression.value,
				settings.useSingleQuotesForStrings ? '\'' : '"',
				true), OperatorPriority.CAST);
	}

	@Override
	public ExpressionString visitConstantUInt(ConstantUIntExpression expression) {
		return new ExpressionString(Integer.toString(expression.value) + " as uint", OperatorPriority.CAST);
	}

	@Override
	public ExpressionString visitConstantULong(ConstantULongExpression expression) {
		return new ExpressionString(Long.toString(expression.value) + " as ulong", OperatorPriority.CAST);
	}

	@Override
	public ExpressionString visitConstantUShort(ConstantUShortExpression expression) {
		return new ExpressionString(Integer.toString(expression.value) + " as ushort", OperatorPriority.CAST);
	}

	@Override
	public ExpressionString visitConstructorThisCall(ConstructorThisCallExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append("this");
		format(result, expression.arguments);
		return new ExpressionString(result.toString(), OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append("super");
		format(result, expression.arguments);
		return new ExpressionString(result.toString(), OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitEnumConstant(EnumConstantExpression expression) {
		return new ExpressionString(expression.type.accept(typeFormatter) + "." + expression.value.name, OperatorPriority.MEMBER);
	}

	@Override
	public ExpressionString visitEquals(EqualsExpression expression) {
		return binary(expression.left, expression.right, OperatorPriority.COMPARE, " === ");
	}

	@Override
	public ExpressionString visitFunction(FunctionExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ExpressionString visitGenericCompare(GenericCompareExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ExpressionString visitGetField(GetFieldExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.target.accept(this));
		result.append('.');
		result.append(expression.field.name);
		return new ExpressionString(result.toString(), OperatorPriority.MEMBER);
	}

	@Override
	public ExpressionString visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		return new ExpressionString(expression.parameter.name, OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitGetLocalVariable(GetLocalVariableExpression expression) {
		return new ExpressionString(expression.variable.name, OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitGetStaticField(GetStaticFieldExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.type.accept(typeFormatter));
		result.append('.');
		result.append(expression.field.name);
		return new ExpressionString(result.toString(), OperatorPriority.MEMBER);
	}

	@Override
	public ExpressionString visitGetter(GetterExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.target.accept(this));
		result.append('.');
		result.append(expression.getter.name);
		return new ExpressionString(result.toString(), OperatorPriority.MEMBER);
	}

	@Override
	public ExpressionString visitInterfaceCast(InterfaceCastExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.value.accept(this).value);
		result.append(" as ");
		result.append(expression.type.accept(typeFormatter));
		return new ExpressionString(result.toString(), OperatorPriority.CAST);
	}

	@Override
	public ExpressionString visitIs(IsExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.value.accept(this).value);
		result.append(" is ");
		result.append(expression.type.accept(typeFormatter));
		return new ExpressionString(result.toString(), OperatorPriority.COMPARE);
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
		return new ExpressionString(result.toString(), OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitNew(NewExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append("new ");
		result.append(expression.type.accept(typeFormatter));
		format(result, expression.arguments);
		return new ExpressionString(result.toString(), OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitNot(NotExpression expression) {
		return unaryPrefix(expression.value, OperatorPriority.NOT, "!");
	}

	@Override
	public ExpressionString visitNull(NullExpression expression) {
		return new ExpressionString("null", OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitOrOr(OrOrExpression expression) {
		return binary(expression.left, expression.right, OperatorPriority.OROR, " || ");
	}

	@Override
	public ExpressionString visitRange(RangeExpression expression) {
		return binary(expression.from, expression.to, OperatorPriority.RANGE, " .. ");
	}

	@Override
	public ExpressionString visitSetField(SetFieldExpression expression) {
		return new ExpressionString(
				expression.target.accept(this) + "." + expression.field.name + " = " + expression.value.accept(this).value, 
				OperatorPriority.ASSIGN);
	}

	@Override
	public ExpressionString visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		return new ExpressionString(
				expression.parameter.name + " = " + expression.value.accept(this).value,
				OperatorPriority.ASSIGN);
	}

	@Override
	public ExpressionString visitSetLocalVariable(SetLocalVariableExpression expression) {
		return new ExpressionString(
				expression.variable.name + " = " + expression.value.accept(this).value,
				OperatorPriority.ASSIGN);
	}

	@Override
	public ExpressionString visitSetStaticField(SetStaticFieldExpression expression) {
		return new ExpressionString(
				expression.type.accept(typeFormatter) + "." + expression.field.name + " = " + expression.value.accept(this).value,
				OperatorPriority.ASSIGN);
	}

	@Override
	public ExpressionString visitSetter(SetterExpression expression) {
		return new ExpressionString(
				expression.target.accept(this) + "." + expression.setter.name + " = " + expression.value.accept(this),
				OperatorPriority.ASSIGN);
	}

	@Override
	public ExpressionString visitStaticGetter(StaticGetterExpression expression) {
		return new ExpressionString(
				expression.type.accept(typeFormatter) + "." + expression.getter.name, 
				OperatorPriority.MEMBER);
	}

	@Override
	public ExpressionString visitStaticSetter(StaticSetterExpression expression) {
		return new ExpressionString(
				expression.type.accept(typeFormatter) + "." + expression.setter.name + " = " + expression.setter.name,
				OperatorPriority.ASSIGN);
	}

	@Override
	public ExpressionString visitThis(ThisExpression expression) {
		return new ExpressionString("this", OperatorPriority.PRIMARY);
	}

	@Override
	public ExpressionString visitWrapOptional(WrapOptionalExpression expression) {
		return expression.value.accept(this);
	}
	
	private ExpressionString unaryPrefix(Expression value, OperatorPriority operator, String operatorString) {
		return new ExpressionString(operatorString + value.accept(this).value, operator);
	}
	
	private ExpressionString unaryPostfix(Expression value, OperatorPriority operator, String operatorString) {
		return new ExpressionString(value.accept(this).value + operatorString, operator);
	}
	
	private ExpressionString binary(Expression left, Expression right, OperatorPriority operator, String operatorString) {
		String value = left.accept(this).wrapLeft(operator)
				+ operatorString + right.accept(this).wrapRight(operator);
		return new ExpressionString(value, operator);
	}
}
