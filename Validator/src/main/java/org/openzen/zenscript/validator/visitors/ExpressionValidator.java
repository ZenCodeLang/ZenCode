/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
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
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.GenericCompareExpression;
import org.openzen.zenscript.codemodel.expression.GetFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.GetStaticFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetterExpression;
import org.openzen.zenscript.codemodel.expression.GlobalCallExpression;
import org.openzen.zenscript.codemodel.expression.GlobalExpression;
import org.openzen.zenscript.codemodel.expression.InterfaceCastExpression;
import org.openzen.zenscript.codemodel.expression.IsExpression;
import org.openzen.zenscript.codemodel.expression.MakeConstExpression;
import org.openzen.zenscript.codemodel.expression.MapExpression;
import org.openzen.zenscript.codemodel.expression.NewExpression;
import org.openzen.zenscript.codemodel.expression.NullExpression;
import org.openzen.zenscript.codemodel.expression.OrOrExpression;
import org.openzen.zenscript.codemodel.expression.PostCallExpression;
import org.openzen.zenscript.codemodel.expression.RangeExpression;
import org.openzen.zenscript.codemodel.expression.SameObjectExpression;
import org.openzen.zenscript.codemodel.expression.SetFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.SetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.SetStaticFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticSetterExpression;
import org.openzen.zenscript.codemodel.expression.SupertypeCastExpression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.expression.WrapOptionalExpression;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class ExpressionValidator implements ExpressionVisitor<Boolean> {
	private final Validator validator;
	private final ExpressionScope scope;
	
	public ExpressionValidator(Validator validator, ExpressionScope scope) {
		this.validator = validator;
		this.scope = scope;
	}

	@Override
	public Boolean visitAndAnd(AndAndExpression expression) {
		boolean isValid =
				expression.left.accept(this)
				& expression.right.accept(this);
		
		if (expression.left.type != BasicTypeID.BOOL) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_OPERAND_TYPE,
					expression.position,
					"left hand side operand of && must be a bool");
			isValid = false;
		}
		if (expression.right.type != BasicTypeID.BOOL) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_OPERAND_TYPE,
					expression.position,
					"right hand side operand of && must be a bool");
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitArray(ArrayExpression expression) {
		boolean isValid = true;
		for (Expression element : expression.expressions) {
			if (element.type != expression.arrayType.elementType) {
				validator.logError(
					ValidationLogEntry.Code.INVALID_OPERAND_TYPE,
					expression.position,
					"array element expression type doesn't match array type");
				isValid = false;
			}
			isValid &= element.accept(this);
		}
		return isValid;
	}

	@Override
	public Boolean visitCompare(BasicCompareExpression expression) {
		boolean isValid = true;
		if (expression.left.type != expression.right.type) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "comparison must be between the same types");
			isValid = false;
		}
		isValid &= expression.left.accept(this);
		isValid &= expression.right.accept(this);
		return isValid;
	}

	@Override
	public Boolean visitCall(CallExpression expression) {
		boolean isValid = true;
		isValid &= expression.target.accept(this);
		isValid &= checkCallArguments(expression.position, expression.member.header, expression.instancedHeader, expression.arguments);
		return isValid;
	}

	@Override
	public Boolean visitCallStatic(CallStaticExpression expression) {
		return checkCallArguments(expression.position, expression.member.header, expression.instancedHeader, expression.arguments);
	}

	@Override
	public Boolean visitCapturedClosure(CapturedClosureExpression expression) {
		return true;
	}

	@Override
	public Boolean visitCapturedDirect(CapturedDirectExpression expression) {
		return true;
	}

	@Override
	public Boolean visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
		return true;
	}

	@Override
	public Boolean visitCapturedParameter(CapturedParameterExpression expression) {
		return true;
	}

	@Override
	public Boolean visitCapturedThis(CapturedThisExpression expression) {
		return true;
	}

	@Override
	public Boolean visitCast(CastExpression expression) {
		return expression.target.accept(this);
	}

	@Override
	public Boolean visitCheckNull(CheckNullExpression expression) {
		boolean isValid = true;
		isValid &= expression.value.accept(this);
		if (!expression.value.type.isOptional()) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "target of a null check is not optional");
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitCoalesce(CoalesceExpression expression) {
		boolean isValid = true;
		isValid &= expression.left.accept(this);
		isValid &= expression.right.accept(this);
		if (!expression.left.type.isOptional()) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "target of a null coalesce is not optional");
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitConditional(ConditionalExpression expression) {
		boolean isValid = true;
		isValid &= expression.condition.accept(this);
		isValid &= expression.ifThen.accept(this);
		isValid &= expression.ifElse.accept(this);
		if (expression.condition.type != BasicTypeID.BOOL) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "conditional expression condition must be a bool");
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitConstantBool(ConstantBoolExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantByte(ConstantByteExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantChar(ConstantCharExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantDouble(ConstantDoubleExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantFloat(ConstantFloatExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantInt(ConstantIntExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantLong(ConstantLongExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantSByte(ConstantSByteExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantShort(ConstantShortExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantString(ConstantStringExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantUInt(ConstantUIntExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantULong(ConstantULongExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstantUShort(ConstantUShortExpression expression) {
		return true;
	}

	@Override
	public Boolean visitConstructorThisCall(ConstructorThisCallExpression expression) {
		boolean isValid = true;
		if (!scope.isConstructor()) {
			validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_OUTSIDE_CONSTRUCTOR, expression.position, "Can only forward constructors inside constructors");
			isValid = false;
		}
		if (!scope.isFirstStatement()) {
			validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_NOT_FIRST_STATEMENT, expression.position, "Constructor forwarder must be first expression");
			isValid = false;
		}
		scope.markConstructorForwarded();
		isValid &= checkCallArguments(expression.position, expression.constructor.header, expression.constructor.header, expression.arguments);
		return isValid;
	}

	@Override
	public Boolean visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		boolean isValid = true;
		if (!scope.isConstructor()) {
			validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_OUTSIDE_CONSTRUCTOR, expression.position, "Can only forward constructors inside constructors");
			isValid = false;
		}
		if (!scope.isFirstStatement()) {
			validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_NOT_FIRST_STATEMENT, expression.position, "Constructor forwarder must be first expression");
			isValid = false;
		}
		scope.markConstructorForwarded();
		isValid &= checkCallArguments(expression.position, expression.constructor.header, expression.constructor.header, expression.arguments);
		return isValid;
	}

	@Override
	public Boolean visitEnumConstant(EnumConstantExpression expression) {
		if (!scope.isEnumConstantInitialized(expression.value)) {
			validator.logError(
					ValidationLogEntry.Code.ENUM_CONSTANT_NOT_YET_INITIALIZED,
					expression.position,
					"Using an enum constant that is not yet initialized: " + expression.value.name);
			return false;
		}
		return true;
	}

	@Override
	public Boolean visitFunction(FunctionExpression expression) {
		// TODO
		return true;
	}

	@Override
	public Boolean visitGenericCompare(GenericCompareExpression expression) {
		boolean isValid = expression.value.accept(this);
		if (expression.value.type != BasicTypeID.INT) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_OPERAND_TYPE,
					expression.position,
					"Generic compare expression must be an int");
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitGetField(GetFieldExpression expression) {
		boolean isValid = true;
		isValid &= expression.target.accept(this);
		if (expression.target instanceof ThisExpression && !scope.isFieldInitialized(expression.field)) {
			validator.logError(
					ValidationLogEntry.Code.FIELD_NOT_YET_INITIALIZED,
					expression.position,
					"Using a field that was not yet initialized");
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		return true;
	}

	@Override
	public Boolean visitGetLocalVariable(GetLocalVariableExpression expression) {
		boolean isValid = true;
		if (!scope.isLocalVariableInitialized(expression.variable)) {
			validator.logError(ValidationLogEntry.Code.LOCAL_VARIABLE_NOT_YET_INITIALIZED, expression.position, "Local variable not yet initialized");
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitGetStaticField(GetStaticFieldExpression expression) {
		return true;
	}

	@Override
	public Boolean visitGetter(GetterExpression expression) {
		return expression.target.accept(this);
	}
	
	@Override
	public Boolean visitGlobal(GlobalExpression expression) {
		return expression.resolution.accept(this);
	}
	
	@Override
	public Boolean visitGlobalCall(GlobalCallExpression expression) {
		return expression.resolution.accept(this);
	}

	@Override
	public Boolean visitInterfaceCast(InterfaceCastExpression expression) {
		boolean isValid = true;
		isValid &= expression.value.accept(this);
		isValid &= expression.type.accept(new TypeValidator(validator, expression.position));
		return isValid;
	}

	@Override
	public Boolean visitIs(IsExpression expression) {
		boolean isValid = true;
		isValid &= expression.value.accept(this);
		isValid &= expression.isType.accept(new TypeValidator(validator, expression.position));
		return isValid;
	}

	@Override
	public Boolean visitMakeConst(MakeConstExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Boolean visitMap(MapExpression expression) {
		boolean isValid = true;
		AssocTypeID type = (AssocTypeID) expression.type;
		for (int i = 0; i < expression.keys.length; i++) {
			Expression key = expression.keys[i];
			Expression value = expression.values[i];
			
			isValid &= key.accept(this);
			isValid &= value.accept(this);
			if (key.type != type.keyType) {
				validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, key.position, "Key type must match the associative array key type");
				isValid = false;
			}
			if (value.type != type.valueType) {
				validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, key.position, "Value type must match the associative array value type");
				isValid = false;
			}
		}
		return isValid;
	}

	@Override
	public Boolean visitNew(NewExpression expression) {
		boolean isValid = checkCallArguments(
				expression.position,
				expression.constructor.header,
				expression.constructor.header,
				expression.arguments);
		return isValid;
	}

	@Override
	public Boolean visitNull(NullExpression expression) {
		return true;
	}

	@Override
	public Boolean visitOrOr(OrOrExpression expression) {
		boolean isValid = true;
		isValid &= expression.left.accept(this);
		isValid &= expression.right.accept(this);
		if (expression.left.type != BasicTypeID.BOOL) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "Left hand side of || expression is not a bool");
			isValid = false;
		}
		if (expression.right.type != BasicTypeID.BOOL) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "Right hand side of || expression is not a bool");
			isValid = false;
		}
		return isValid;
	}
	
	@Override
	public Boolean visitPostCall(PostCallExpression expression) {
		boolean isValid = true;
		expression.target.accept(this);
		// TODO: is target a valid increment target?
		return isValid;
	}

	@Override
	public Boolean visitRange(RangeExpression expression) {
		boolean isValid = true;
		isValid &= expression.from.accept(this);
		isValid &= expression.to.accept(this);
		
		RangeTypeID rangeType = (RangeTypeID) expression.type;
		if (expression.from.type != rangeType.from) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "From operand is not a " + rangeType.from.toString());
			isValid = false;
		}
		if (expression.to.type != rangeType.to) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "To operand is not a " + rangeType.to.toString());
			isValid = false;
		}
		return isValid;
	}
	
	@Override
	public Boolean visitSameObject(SameObjectExpression expression) {
		boolean isValid = true;
		isValid &= expression.left.accept(this);
		isValid &= expression.right.accept(this);
		return isValid;
	}

	@Override
	public Boolean visitSetField(SetFieldExpression expression) {
		boolean isValid = true;
		isValid &= expression.target.accept(this);
		isValid &= expression.value.accept(this);
		if (expression.value.type != expression.field.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE, 
					expression.position,
					"Trying to set a field of type " + expression.field.type + " to a value of type " + expression.value.type);
			isValid = false;
		}
		if (expression.field.isFinal()) {
			if (!(expression.target instanceof ThisExpression && scope.isConstructor())) {
				validator.logError(ValidationLogEntry.Code.SETTING_FINAL_FIELD, expression.position, "Cannot set a final field");
				isValid = false;
			}
		}
		return isValid;
	}

	@Override
	public Boolean visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		boolean isValid = true;
		isValid &= expression.value.accept(this);
		if (expression.value.type != expression.parameter.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE, 
					expression.position,
					"Trying to set a parameter of type " + expression.parameter.type + " to a value of type " + expression.value.type);
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitSetLocalVariable(SetLocalVariableExpression expression) {
		boolean isValid = true;
		isValid &= expression.value.accept(this);
		if (expression.value.type != expression.variable.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE, 
					expression.position,
					"Trying to set a variable of type " + expression.variable.type + " to a value of type " + expression.value.type);
			isValid = false;
		}
		if (expression.variable.isFinal) {
			validator.logError(ValidationLogEntry.Code.SETTING_FINAL_VARIABLE, expression.position, "Trying to set final variable " + expression.variable.name);
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitSetStaticField(SetStaticFieldExpression expression) {
		boolean isValid = true;
		isValid &= expression.value.accept(this);
		if (expression.value.type != expression.field.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE,
					expression.position,
					"Trying to set a static field of type " + expression.field.type + " to a value of type " + expression.value.type);
			isValid = false;
		}
		if (expression.field.isFinal()) {
			if (!scope.isStaticInitializer() || expression.field.definition != scope.getDefinition()) {
				validator.logError(
						ValidationLogEntry.Code.SETTING_FINAL_FIELD,
						expression.position,
						"Trying to set final field " + expression.field.name);
				isValid = false;
			}
		}
		return isValid;
	}

	@Override
	public Boolean visitSetter(SetterExpression expression) {
		boolean isValid = true;
		isValid &= expression.target.accept(this);
		isValid &= expression.value.accept(this);
		if (expression.value.type != expression.setter.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE,
					expression.position,
					"Trying to set a property of type " + expression.setter.type + " to a value of type " + expression.value.type);
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitStaticGetter(StaticGetterExpression expression) {
		return true;
	}

	@Override
	public Boolean visitStaticSetter(StaticSetterExpression expression) {
		boolean isValid = true;
		isValid &= expression.value.accept(this);
		if (expression.value.type != expression.setter.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE,
					expression.position,
					"Trying to set a static property of type " + expression.setter.type + " to a value of type " + expression.value.type);
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitSupertypeCast(SupertypeCastExpression expression) {
		boolean isValid = expression.value.accept(this);
		return isValid;
	}

	@Override
	public Boolean visitThis(ThisExpression expression) {
		if (!scope.hasThis()) {
			validator.logError(ValidationLogEntry.Code.THIS_IN_STATIC_SCOPE, expression.position, "Cannot use this in a static scope");
			return false;
		}
		
		return true;
	}

	@Override
	public Boolean visitWrapOptional(WrapOptionalExpression expression) {
		boolean isValid = expression.value.accept(this);
		if (expression.value.type.isOptional()) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "expression value is already optional");
			isValid = false;
		}
		return isValid;
	}
	
	private boolean checkCallArguments(CodePosition position, FunctionHeader originalHeader, FunctionHeader instancedHeader, CallArguments arguments) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateTypeArguments(validator, position, originalHeader.typeParameters, arguments.typeArguments);
		
		for (int i = 0; i < arguments.arguments.length; i++) {
			Expression argument = arguments.arguments[i];
			isValid &= argument.accept(this);
			
			if (i >= instancedHeader.parameters.length) {
				FunctionParameter variadic = instancedHeader.getVariadicParameter();
				if (variadic == null) {
					validator.logError(ValidationLogEntry.Code.INVALID_CALL_ARGUMENT, position, "too many call arguments");
					isValid = false;
					break;
				} else if (variadic.type instanceof ArrayTypeID) {
					ITypeID elementType = ((ArrayTypeID)variadic.type).elementType;
					if (elementType != argument.type) {
						validator.logError(ValidationLogEntry.Code.INVALID_CALL_ARGUMENT, position, "invalid type for variadic call argument");
						isValid = false;
						break;
					}
				}
			}
			
			FunctionParameter parameter = instancedHeader.parameters[i];
			if (parameter.type != argument.type) {
				validator.logError(
						ValidationLogEntry.Code.INVALID_CALL_ARGUMENT,
						position,
						"invalid value for parameter " + parameter.name + ": " + parameter.type.toString() + " expected but " + arguments.arguments[i].type + " given");
				isValid = false;
			}
		}
		
		for (int i = arguments.arguments.length; i < instancedHeader.parameters.length; i++) {
			FunctionParameter parameter = instancedHeader.parameters[i];
			if (parameter.defaultValue == null && !parameter.variadic) {
				validator.logError(ValidationLogEntry.Code.INVALID_CALL_ARGUMENT, position, "missing call argument for " + parameter.name);
				isValid = false;
			}
		}
		
		return isValid;
	}
}
