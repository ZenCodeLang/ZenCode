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
import org.openzen.zenscript.codemodel.expression.CompareExpression;
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
import org.openzen.zenscript.codemodel.expression.ConstExpression;
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
import org.openzen.zenscript.codemodel.expression.MatchExpression;
import org.openzen.zenscript.codemodel.expression.NewExpression;
import org.openzen.zenscript.codemodel.expression.NullExpression;
import org.openzen.zenscript.codemodel.expression.OrOrExpression;
import org.openzen.zenscript.codemodel.expression.PanicExpression;
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
import org.openzen.zenscript.codemodel.expression.ThrowExpression;
import org.openzen.zenscript.codemodel.expression.TryConvertExpression;
import org.openzen.zenscript.codemodel.expression.TryRethrowAsExceptionExpression;
import org.openzen.zenscript.codemodel.expression.TryRethrowAsResultExpression;
import org.openzen.zenscript.codemodel.expression.VariantValueExpression;
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
public class ExpressionValidator implements ExpressionVisitor<Void> {
	private final Validator validator;
	private final ExpressionScope scope;
	
	public ExpressionValidator(Validator validator, ExpressionScope scope) {
		this.validator = validator;
		this.scope = scope;
	}

	@Override
	public Void visitAndAnd(AndAndExpression expression) {
		expression.left.accept(this);
		expression.right.accept(this);
		
		if (expression.left.type != BasicTypeID.BOOL) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_OPERAND_TYPE,
					expression.position,
					"left hand side operand of && must be a bool");
		}
		if (expression.right.type != BasicTypeID.BOOL) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_OPERAND_TYPE,
					expression.position,
					"right hand side operand of && must be a bool");
		}
		return null;
	}

	@Override
	public Void visitArray(ArrayExpression expression) {
		for (Expression element : expression.expressions) {
			if (element.type != expression.arrayType.elementType) {
				validator.logError(
					ValidationLogEntry.Code.INVALID_OPERAND_TYPE,
					expression.position,
					"array element expression type doesn't match array type");
			}
			element.accept(this);
		}
		return null;
	}

	@Override
	public Void visitCompare(CompareExpression expression) {
		if (expression.right.type != expression.operator.header.parameters[0].type) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "comparison has invalid right type!");
		}
		expression.left.accept(this);
		expression.right.accept(this);
		return null;
	}

	@Override
	public Void visitCall(CallExpression expression) {
		expression.target.accept(this);
		checkCallArguments(expression.position, expression.member.header, expression.instancedHeader, expression.arguments);
		return null;
	}

	@Override
	public Void visitCallStatic(CallStaticExpression expression) {
		checkCallArguments(expression.position, expression.member.header, expression.instancedHeader, expression.arguments);
		return null;
	}
	
	@Override
	public Void visitConst(ConstExpression expression) {
		return null;
	}

	@Override
	public Void visitCapturedClosure(CapturedClosureExpression expression) {
		return null;
	}

	@Override
	public Void visitCapturedDirect(CapturedDirectExpression expression) {
		return null;
	}

	@Override
	public Void visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
		return null;
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		return null;
	}

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		return null;
	}

	@Override
	public Void visitCast(CastExpression expression) {
		return expression.target.accept(this);
	}

	@Override
	public Void visitCheckNull(CheckNullExpression expression) {
		expression.value.accept(this);
		if (!expression.value.type.isOptional()) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "target of a null check is not optional");
		}
		return null;
	}

	@Override
	public Void visitCoalesce(CoalesceExpression expression) {
		expression.left.accept(this);
		expression.right.accept(this);
		if (!expression.left.type.isOptional()) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "target of a null coalesce is not optional");
		}
		return null;
	}

	@Override
	public Void visitConditional(ConditionalExpression expression) {
		expression.condition.accept(this);
		expression.ifThen.accept(this);
		expression.ifElse.accept(this);
		if (expression.condition.type != BasicTypeID.BOOL) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "conditional expression condition must be a bool");
		}
		return null;
	}

	@Override
	public Void visitConstantBool(ConstantBoolExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantByte(ConstantByteExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantChar(ConstantCharExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantDouble(ConstantDoubleExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantFloat(ConstantFloatExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantInt(ConstantIntExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantLong(ConstantLongExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantSByte(ConstantSByteExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantShort(ConstantShortExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantString(ConstantStringExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantUInt(ConstantUIntExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantULong(ConstantULongExpression expression) {
		return null;
	}

	@Override
	public Void visitConstantUShort(ConstantUShortExpression expression) {
		return null;
	}

	@Override
	public Void visitConstructorThisCall(ConstructorThisCallExpression expression) {
		if (!scope.isConstructor()) {
			validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_OUTSIDE_CONSTRUCTOR, expression.position, "Can only forward constructors inside constructors");
		}
		if (!scope.isFirstStatement()) {
			validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_NOT_FIRST_STATEMENT, expression.position, "Constructor forwarder must be first expression");
		}
		scope.markConstructorForwarded();
		checkCallArguments(expression.position, expression.constructor.header, expression.constructor.header, expression.arguments);
		return null;
	}

	@Override
	public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		if (!scope.isConstructor()) {
			validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_OUTSIDE_CONSTRUCTOR, expression.position, "Can only forward constructors inside constructors");
		}
		if (!scope.isFirstStatement()) {
			validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_NOT_FIRST_STATEMENT, expression.position, "Constructor forwarder must be first expression");
		}
		scope.markConstructorForwarded();
		checkCallArguments(expression.position, expression.constructor.header, expression.constructor.header, expression.arguments);
		return null;
	}

	@Override
	public Void visitEnumConstant(EnumConstantExpression expression) {
		if (!scope.isEnumConstantInitialized(expression.value)) {
			validator.logError(
					ValidationLogEntry.Code.ENUM_CONSTANT_NOT_YET_INITIALIZED,
					expression.position,
					"Using an enum constant that is not yet initialized: " + expression.value.name);
		}
		return null;
	}

	@Override
	public Void visitFunction(FunctionExpression expression) {
		// TODO
		return null;
	}

	@Override
	public Void visitGetField(GetFieldExpression expression) {
		expression.target.accept(this);
		if (expression.target instanceof ThisExpression && !scope.isFieldInitialized(expression.field.member)) {
			validator.logError(
					ValidationLogEntry.Code.FIELD_NOT_YET_INITIALIZED,
					expression.position,
					"Using a field that was not yet initialized");
		}
		return null;
	}

	@Override
	public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		return null;
	}

	@Override
	public Void visitGetLocalVariable(GetLocalVariableExpression expression) {
		if (!scope.isLocalVariableInitialized(expression.variable)) {
			validator.logError(ValidationLogEntry.Code.LOCAL_VARIABLE_NOT_YET_INITIALIZED, expression.position, "Local variable not yet initialized");
		}
		return null;
	}

	@Override
	public Void visitGetStaticField(GetStaticFieldExpression expression) {
		return null;
	}

	@Override
	public Void visitGetter(GetterExpression expression) {
		return expression.target.accept(this);
	}
	
	@Override
	public Void visitGlobal(GlobalExpression expression) {
		return expression.resolution.accept(this);
	}
	
	@Override
	public Void visitGlobalCall(GlobalCallExpression expression) {
		return expression.resolution.accept(this);
	}

	@Override
	public Void visitInterfaceCast(InterfaceCastExpression expression) {
		expression.value.accept(this);
		expression.type.accept(new TypeValidator(validator, expression.position));
		return null;
	}

	@Override
	public Void visitIs(IsExpression expression) {
		expression.value.accept(this);
		expression.isType.accept(new TypeValidator(validator, expression.position));
		return null;
	}

	@Override
	public Void visitMakeConst(MakeConstExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Void visitMap(MapExpression expression) {
		AssocTypeID type = (AssocTypeID) expression.type;
		for (int i = 0; i < expression.keys.length; i++) {
			Expression key = expression.keys[i];
			Expression value = expression.values[i];
			
			key.accept(this);
			value.accept(this);
			if (key.type != type.keyType) {
				validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, key.position, "Key type must match the associative array key type");
			}
			if (value.type != type.valueType) {
				validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, key.position, "Value type must match the associative array value type");
			}
		}
		return null;
	}

	@Override
	public Void visitMatch(MatchExpression expression) {
		// TODO
		return null;
	}

	@Override
	public Void visitNew(NewExpression expression) {
		checkCallArguments(
				expression.position,
				expression.constructor.header,
				expression.instancedHeader,
				expression.arguments);
		return null;
	}

	@Override
	public Void visitNull(NullExpression expression) {
		return null;
	}

	@Override
	public Void visitOrOr(OrOrExpression expression) {
		expression.left.accept(this);
		expression.right.accept(this);
		if (expression.left.type != BasicTypeID.BOOL) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "Left hand side of || expression is not a bool");
		}
		if (expression.right.type != BasicTypeID.BOOL) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "Right hand side of || expression is not a bool");
		}
		return null;
	}
	
	@Override
	public Void visitPanic(PanicExpression expression) {
		expression.value.accept(this);
		if (expression.type != BasicTypeID.STRING)
			validator.logError(ValidationLogEntry.Code.PANIC_ARGUMENT_NO_STRING, expression.position, "Argument to a panic must be a string");
		return null;
	}
	
	@Override
	public Void visitPostCall(PostCallExpression expression) {
		expression.target.accept(this);
		// TODO: is target a valid increment target?
		return null;
	}

	@Override
	public Void visitRange(RangeExpression expression) {
		expression.from.accept(this);
		expression.to.accept(this);
		
		RangeTypeID rangeType = (RangeTypeID) expression.type;
		if (expression.from.type != rangeType.from) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "From operand is not a " + rangeType.from.toString());
		}
		if (expression.to.type != rangeType.to) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "To operand is not a " + rangeType.to.toString());
		}
		return null;
	}
	
	@Override
	public Void visitSameObject(SameObjectExpression expression) {
		expression.left.accept(this);
		expression.right.accept(this);
		return null;
	}

	@Override
	public Void visitSetField(SetFieldExpression expression) {
		expression.target.accept(this);
		expression.value.accept(this);
		if (expression.value.type != expression.field.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE, 
					expression.position,
					"Trying to set a field of type " + expression.field.type + " to a value of type " + expression.value.type);
		}
		if (expression.field.isFinal()) {
			if (!(expression.target instanceof ThisExpression && scope.isConstructor())) {
				validator.logError(ValidationLogEntry.Code.SETTING_FINAL_FIELD, expression.position, "Cannot set a final field");
			}
		}
		return null;
	}

	@Override
	public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		expression.value.accept(this);
		if (expression.value.type != expression.parameter.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE, 
					expression.position,
					"Trying to set a parameter of type " + expression.parameter.type + " to a value of type " + expression.value.type);
		}
		return null;
	}

	@Override
	public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
		expression.value.accept(this);
		if (expression.value.type != expression.variable.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE, 
					expression.position,
					"Trying to set a variable of type " + expression.variable.type + " to a value of type " + expression.value.type);
		}
		if (expression.variable.isFinal) {
			validator.logError(ValidationLogEntry.Code.SETTING_FINAL_VARIABLE, expression.position, "Trying to set final variable " + expression.variable.name);
		}
		return null;
	}

	@Override
	public Void visitSetStaticField(SetStaticFieldExpression expression) {
		expression.value.accept(this);
		if (expression.value.type != expression.field.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE,
					expression.position,
					"Trying to set a static field of type " + expression.field.type + " to a value of type " + expression.value.type);
		}
		if (expression.field.isFinal()) {
			if (!scope.isStaticInitializer() || expression.field.member.definition != scope.getDefinition()) {
				validator.logError(
						ValidationLogEntry.Code.SETTING_FINAL_FIELD,
						expression.position,
						"Trying to set final field " + expression.field.member.name);
			}
		}
		return null;
	}

	@Override
	public Void visitSetter(SetterExpression expression) {
		expression.target.accept(this);
		expression.value.accept(this);
		if (expression.value.type != expression.setter.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE,
					expression.position,
					"Trying to set a property of type " + expression.setter.type + " to a value of type " + expression.value.type);
		}
		return null;
	}

	@Override
	public Void visitStaticGetter(StaticGetterExpression expression) {
		return null;
	}

	@Override
	public Void visitStaticSetter(StaticSetterExpression expression) {
		expression.value.accept(this);
		if (expression.value.type != expression.setter.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE,
					expression.position,
					"Trying to set a static property of type " + expression.setter.type + " to a value of type " + expression.value.type);
		}
		return null;
	}

	@Override
	public Void visitSupertypeCast(SupertypeCastExpression expression) {
		expression.value.accept(this);
		return null;
	}

	@Override
	public Void visitThis(ThisExpression expression) {
		if (!scope.hasThis()) {
			validator.logError(ValidationLogEntry.Code.THIS_IN_STATIC_SCOPE, expression.position, "Cannot use this in a static scope");
		}
		
		return null;
	}
	
	@Override
	public Void visitThrow(ThrowExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Void visitTryConvert(TryConvertExpression expression) {
		expression.value.accept(this);
		return null;
	}

	@Override
	public Void visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
		expression.value.accept(this);
		return null;
	}
	
	@Override
	public Void visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
		expression.value.accept(this);
		return null;
	}

	@Override
	public Void visitVariantValue(VariantValueExpression expression) {
		if (expression.getNumberOfArguments() != expression.option.types.length) {
			validator.logError(ValidationLogEntry.Code.INVALID_CALL_ARGUMENT, expression.position, "Invalid number of variant arguments for variant element " + expression.option.getName());
		}
		for (int i = 0; i < expression.getNumberOfArguments(); i++) {
			if (expression.arguments[i].type != expression.option.types[i]) {
				validator.logError(
						ValidationLogEntry.Code.INVALID_CALL_ARGUMENT,
						expression.position,
						"Invalid variant argument for argument " + i + ": " + expression.arguments[i].type + " given but " + expression.option.types[i] + " expected");
			}
		}
		return null;
	}

	@Override
	public Void visitWrapOptional(WrapOptionalExpression expression) {
		expression.value.accept(this);
		if (expression.value.type.isOptional()) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "expression value is already optional");
		}
		return null;
	}
	
	private void checkCallArguments(CodePosition position, FunctionHeader originalHeader, FunctionHeader instancedHeader, CallArguments arguments) {
		ValidationUtils.validateTypeArguments(validator, position, originalHeader.typeParameters, arguments.typeArguments);
		
		for (int i = 0; i < arguments.arguments.length; i++) {
			Expression argument = arguments.arguments[i];
			argument.accept(this);
			
			if (i >= instancedHeader.parameters.length) {
				FunctionParameter variadic = instancedHeader.getVariadicParameter();
				if (variadic == null) {
					validator.logError(ValidationLogEntry.Code.INVALID_CALL_ARGUMENT, position, "too many call arguments");
					break;
				} else if (variadic.type instanceof ArrayTypeID) {
					ITypeID elementType = ((ArrayTypeID)variadic.type).elementType;
					if (elementType != argument.type) {
						validator.logError(ValidationLogEntry.Code.INVALID_CALL_ARGUMENT, position, "invalid type for variadic call argument");
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
			}
		}
		
		for (int i = arguments.arguments.length; i < instancedHeader.parameters.length; i++) {
			FunctionParameter parameter = instancedHeader.parameters[i];
			if (parameter.defaultValue == null && !parameter.variadic) {
				validator.logError(ValidationLogEntry.Code.INVALID_CALL_ARGUMENT, position, "missing call argument for " + parameter.name);
			}
		}
	}
}
