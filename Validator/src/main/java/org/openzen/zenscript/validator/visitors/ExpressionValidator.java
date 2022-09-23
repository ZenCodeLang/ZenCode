/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.EnumConstantSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.validator.TypeContext;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
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
			if (!element.type.equals(expression.arrayType.elementType)) {
				validator.logError(
						ValidationLogEntry.Code.INVALID_OPERAND_TYPE,
						expression.position,
						"array element expression type " + element.type + " doesn't match array type " + expression.arrayType.elementType);
			}
			element.accept(this);
		}
		return null;
	}

	@Override
	public Void visitCompare(CompareExpression expression) {
		if (expression.operator.getHeader().parameters.length == 0)
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "comparison operator has no parameters!");
		else if (!expression.right.type.equals(expression.operator.getHeader().parameters[0].type))
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "comparison has invalid right type!");

		checkMemberAccess(expression.position, expression.operator);
		checkNotStatic(expression.position, expression.operator);

		expression.left.accept(this);
		expression.right.accept(this);
		return null;
	}

	@Override
	public Void visitCall(CallExpression expression) {
		expression.target.accept(this);
		checkMemberAccess(expression.position, expression.method);

		// ToDo: This probably needs to be done elsewhere?
		//   Reason: INT_TO_STRING as method is (int): string
		//   But the call is <int>.caster()
		//   Hence the checkCallArguments method will say that () != (int) argument mismatch!
		//final CallArguments arguments = expression.method.getID().getKind() == MethodID.Kind.CASTER
		//		? expression.arguments.bind(expression.target)
		//		: expression.arguments;
		//checkCallArguments(expression.position, expression.method.method.getHeader(), expression.method.getHeader(), arguments);
		checkCallArguments(expression.position, expression.method.method.getHeader(), expression.method.getHeader(), expression.arguments);
		checkNotStatic(expression.position, expression.method);

		MethodID id = expression.method.getID();
		switch (id.getKind()) {
			case INSTANCEMETHOD:
			case OPERATOR:
			case GETTER:
			case SETTER:
			case CASTER:
				return null;
			default:
				validator.logError(ValidationLogEntry.Code.INVALID_METHOD_TYPE, expression.position, "Invalid method type: " + id.getKind());
				return null;
		}
	}

	@Override
	public Void visitCallStatic(CallStaticExpression expression) {
		checkMemberAccess(expression.position, expression.member);
		checkCallArguments(expression.position, expression.member.method.getHeader(), expression.member.getHeader(), expression.arguments);
		checkStatic(expression.position, expression.member);

		MethodID id = expression.member.getID();
		switch (id.getKind()) {
			case STATICOPERATOR:
			case STATICMETHOD:
			case STATICGETTER:
			case STATICSETTER:
				return null;
			default:
				validator.logError(ValidationLogEntry.Code.INVALID_METHOD_TYPE, expression.position, "Invalid method type: " + id.getKind());
				return null;
		}
	}

	@Override
	public Void visitCapturedClosure(CapturedClosureExpression expression) {
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
	public Void visitConstantUSize(ConstantUSizeExpression expression) {
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
		checkCallArguments(expression.position, expression.constructor.getHeader(), expression.constructor.getHeader(), expression.arguments);
		return null;
	}

	@Override
	public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		checkMemberAccess(expression.position, expression.constructor);

		if (!scope.isConstructor()) {
			validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_OUTSIDE_CONSTRUCTOR, expression.position, "Can only forward constructors inside constructors");
		}
		if (!scope.isFirstStatement()) {
			validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_NOT_FIRST_STATEMENT, expression.position, "Constructor forwarder must be first expression");
		}
		scope.markConstructorForwarded();
		checkCallArguments(expression.position, expression.constructor.getHeader(), expression.constructor.getHeader(), expression.arguments);
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
		checkFieldAccess(expression.position, expression.field.field);
		checkNotStatic(expression.position, expression.field);

		expression.target.accept(this);
		if (expression.target instanceof ThisExpression && !scope.isFieldInitialized(expression.field.field)) {
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
	public Void visitGetMatchingVariantField(GetMatchingVariantField expression) {
		if (expression.index >= expression.value.parameters.length) {
			validator.logError(ValidationLogEntry.Code.MATCHING_VARIANT_FIELD_INVALID, expression.position, "Invalid matching variant field");
		}
		return null;
	}

	@Override
	public Void visitGetStaticField(GetStaticFieldExpression expression) {
		checkFieldAccess(expression.position, expression.field.field);
		checkStatic(expression.position, expression.field);
		return null;
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
		new TypeValidator(validator, expression.position).validate(TypeContext.CAST_TARGET_TYPE, expression.type);
		return null;
	}

	@Override
	public Void visitInvalid(InvalidExpression expression) {
		validator.logError(ValidationLogEntry.Code.INVALID_EXPRESSION, expression.position, expression.error.description);
		return null;
	}

	@Override
	public Void visitInvalidAssign(InvalidAssignExpression expression) {
		expression.target.accept(this);
		expression.source.accept(this);
		return null;
	}

	@Override
	public Void visitIs(IsExpression expression) {
		expression.value.accept(this);
		new TypeValidator(validator, expression.position).validate(TypeContext.TYPE_CHECK_TYPE, expression.isType);
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
			if (!key.type.equals(type.keyType)) {
				validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, key.position, "Key type " + key.type + " must match the associative array key type " + type.keyType);
			}
			if (!value.type.equals(type.valueType)) {
				validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, key.position, "Value type " + value.type + " must match the associative array value type " + type.valueType);
			}
		}
		return null;
	}

	@Override
	public Void visitMatch(MatchExpression expression) {
		expression.value.accept(this);
		for (MatchExpression.Case case_ : expression.cases) {
			case_.value.accept(this);
		}

		boolean hasDefault = false;
		if (expression.value.type.isVariant()) {
			Set<VariantDefinition.Option> options = new HashSet<>();
			for (MatchExpression.Case case_ : expression.cases) {
				if (case_.key == null) {
					if (hasDefault)
						validator.logError(ValidationLogEntry.Code.DUPLICATE_DEFAULT_CASE, expression.position, "Duplicate default in match");

					hasDefault = true;
				} else if (case_.key instanceof VariantOptionSwitchValue) {
					VariantDefinition.Option option = ((VariantOptionSwitchValue) case_.key).option.getOption();
					if (options.contains(option))
						validator.logError(ValidationLogEntry.Code.DUPLICATE_CASE, expression.position, "Duplicate case in match: " + option.name);

					options.add(option);
				} else {
					validator.logError(ValidationLogEntry.Code.INVALID_CASE, expression.position, "Invalid case: must be default or option value");
				}
			}

			if (!hasDefault) {
				VariantDefinition variant = (VariantDefinition) ((DefinitionTypeID) expression.value.type).definition;
				for (VariantDefinition.Option option : variant.options) {
					if (!options.contains(option))
						validator.logError(ValidationLogEntry.Code.INCOMPLETE_MATCH, expression.position, "Incomplete match: missing option for " + option.name);
				}
			}
		} else if (expression.type.isEnum()) {
			Set<EnumConstantMember> options = new HashSet<>();
			for (MatchExpression.Case case_ : expression.cases) {
				if (case_.key == null) {
					if (hasDefault)
						validator.logError(ValidationLogEntry.Code.DUPLICATE_DEFAULT_CASE, expression.position, "Duplicate default in match");

					hasDefault = true;
				} else if (case_.key instanceof EnumConstantSwitchValue) {
					EnumConstantMember option = ((EnumConstantSwitchValue) case_.key).constant;
					if (options.contains(option))
						validator.logError(ValidationLogEntry.Code.DUPLICATE_CASE, expression.position, "Duplicate case in match: " + option.name);

					options.add(option);
				} else {
					validator.logError(ValidationLogEntry.Code.INVALID_CASE, expression.position, "Invalid case: must be default or enum value");
				}
			}

			if (!hasDefault) {
				EnumDefinition enum_ = (EnumDefinition) ((DefinitionTypeID) expression.value.type).definition;
				for (EnumConstantMember option : enum_.enumConstants) {
					if (!options.contains(option))
						validator.logError(ValidationLogEntry.Code.INCOMPLETE_MATCH, expression.position, "Incomplete match: missing option for " + option.name);
				}
			}
		} else {
			for (MatchExpression.Case case_ : expression.cases) {
				if (case_.key == null) {
					if (hasDefault)
						validator.logError(ValidationLogEntry.Code.DUPLICATE_DEFAULT_CASE, expression.position, "Duplicate default in match");

					hasDefault = true;
				}
			}

			if (!hasDefault)
				validator.logError(ValidationLogEntry.Code.INCOMPLETE_MATCH, expression.position, "Incomplete match: must have a default option");
		}
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

		if (expression.left.type != BasicTypeID.BOOL)
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "Left hand side of || expression is not a bool");
		if (expression.right.type != BasicTypeID.BOOL)
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "Right hand side of || expression is not a bool");

		return null;
	}

	@Override
	public Void visitPanic(PanicExpression expression) {
		expression.value.accept(this);
		if (expression.value.type != BasicTypeID.STRING)
			validator.logError(ValidationLogEntry.Code.PANIC_ARGUMENT_NO_STRING, expression.position, "Argument to a panic must be a string");

		return null;
	}

	@Override
	public Void visitPlatformSpecific(Expression expression) {
		// TODO: how to validate these expressions?
		return null;
	}

	@Override
	public Void visitPostCall(PostCallExpression expression) {
		checkMemberAccess(expression.position, expression.member);
		checkNotStatic(expression.position, expression.member);

		expression.target.accept(this);
		// TODO: is target a valid increment target?
		return null;
	}

	@Override
	public Void visitRange(RangeExpression expression) {
		expression.from.accept(this);
		expression.to.accept(this);

		RangeTypeID rangeType = (RangeTypeID) expression.type;
		if (!expression.from.type.equals(rangeType.baseType)) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "From operand is not a " + rangeType.baseType.toString());
		}
		if (!expression.to.type.equals(rangeType.baseType)) {
			validator.logError(ValidationLogEntry.Code.INVALID_OPERAND_TYPE, expression.position, "To operand is not a " + rangeType.baseType.toString());
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
		checkFieldAccess(expression.position, expression.field.field);
		checkNotStatic(expression.position, expression.field);

		expression.target.accept(this);
		expression.value.accept(this);
		if (!expression.value.type.equals(expression.field.getType())) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE,
					expression.position,
					"Trying to set a field of type " + expression.field.getType() + " to a value of type " + expression.value.type);
		}
		if (expression.field.getModifiers().isFinal()) {
			if (!(expression.target instanceof ThisExpression && scope.isConstructor())) {
				validator.logError(ValidationLogEntry.Code.SETTING_FINAL_FIELD, expression.position, "Cannot set a final field");
			}
		}
		return null;
	}

	@Override
	public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		expression.value.accept(this);
		if (!expression.value.type.equals(expression.parameter.type)) {
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
		if (!expression.value.type.equals(expression.variable.type)) {
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
		checkFieldAccess(expression.position, expression.field.field);
		checkStatic(expression.position, expression.field);

		expression.value.accept(this);
		if (!expression.value.type.equals(expression.field.getType())) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_SOURCE_TYPE,
					expression.position,
					"Trying to set a static field of type " + expression.field.getType() + " to a value of type " + expression.value.type);
		}
		if (expression.field.getModifiers().isFinal()) {
			if (!scope.isStaticInitializer() || expression.field.field.getDefiningType() != scope.getDefinition()) {
				validator.logError(
						ValidationLogEntry.Code.SETTING_FINAL_FIELD,
						expression.position,
						"Trying to set final field " + expression.field.field.getName());
			}
		}
		return null;
	}

	@Override
	public Void visitSupertypeCast(SupertypeCastExpression expression) {
		expression.value.accept(this);
		return null;
	}

	@Override
	public Void visitSubtypeCast(SubtypeCastExpression expression) {
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
			if (!expression.arguments[i].type.equals(expression.option.types[i])) {
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

	private void checkMemberAccess(CodePosition position, MethodInstance member) {
		if (!hasAccess(member.getModifiers(), member.method.getDefiningType())) {
			validator.logError(ValidationLogEntry.Code.NO_ACCESS, position, "no access to " + member.getID());
		}
	}

	private void checkFieldAccess(CodePosition position, FieldSymbol field) {
		if (!hasAccess(field.getModifiers(), field.getDefiningType()))
			validator.logError(ValidationLogEntry.Code.NO_ACCESS, position, "no field access to " + field.getName());
	}

	private boolean hasAccess(Modifiers modifiers, DefinitionSymbol definition) {
		if (modifiers.isPrivate())
			return definition == scope.getDefinition();
		if (modifiers.isProtected())
			return definition.asType()
					.map(type -> DefinitionTypeID.createThis(scope.getDefinition()).extendsOrImplements(DefinitionTypeID.createThis(type)))
					.orElse(false);
		if (modifiers.isInternal())
			return definition.getModule() == validator.module;

		return true;
	}

	private void checkStatic(CodePosition position, MethodInstance member) {
		if (!member.getModifiers().isStatic())
			validator.logError(ValidationLogEntry.Code.MUST_BE_STATIC, position, "Member is not static");
	}

	private void checkStatic(CodePosition position, FieldInstance member) {
		if (!member.getModifiers().isStatic())
			validator.logError(ValidationLogEntry.Code.MUST_BE_STATIC, position, "Member is not static");
	}

	private void checkNotStatic(CodePosition position, FieldInstance member) {
		if (member.getModifiers().isStatic())
			validator.logError(ValidationLogEntry.Code.MUST_NOT_BE_STATIC, position, "Member must not be static");
	}

	private void checkNotStatic(CodePosition position, MethodInstance member) {
		if (member.getModifiers().isStatic())
			validator.logError(ValidationLogEntry.Code.MUST_NOT_BE_STATIC, position, "Member must not be static");
	}

	private void checkCallArguments(CodePosition position, FunctionHeader originalHeader, FunctionHeader instancedHeader, CallArguments arguments) {
		ValidationUtils.validateTypeArguments(validator, position, originalHeader.typeParameters, arguments.typeArguments);

		boolean isVariadic = instancedHeader.isVariadicCall(arguments);
		for (int i = 0; i < arguments.arguments.length; i++) {
			Expression argument = arguments.arguments[i];
			argument.accept(this);

			if (i >= instancedHeader.parameters.length) {
				Optional<FunctionParameter> variadic = instancedHeader.getVariadicParameter();
				if (!variadic.isPresent()) {
					validator.logError(ValidationLogEntry.Code.INVALID_CALL_ARGUMENT, position, "too many call arguments");
					break;
				} else {
					Optional<ArrayTypeID> maybeArray = variadic.get().type.asArray();
					if (maybeArray.isPresent()) {
						TypeID elementType = maybeArray.get().elementType;
						if (!elementType.equals(argument.type)) {
							validator.logError(ValidationLogEntry.Code.INVALID_CALL_ARGUMENT, position, "invalid type for variadic call argument");
							break;
						}
					} else {
						validator.logError(ValidationLogEntry.Code.INVALID_CALL_ARGUMENT, position, "variadic parameter is not an array");
					}
				}
			}

			FunctionParameter parameter = instancedHeader.getParameter(isVariadic, i);
			if (!parameter.type.equals(argument.type) && (parameter.defaultValue == null || !Objects.equals(parameter.defaultValue.type, argument.type))) {
				if (!parameter.type.equals(argument.type))
					validator.logError(
							ValidationLogEntry.Code.INVALID_CALL_ARGUMENT,
							position,
							"invalid value for parameter " + parameter.name + ": " + parameter.type.toString() + " expected but " + arguments.arguments[i].type + " given");
			}
		}

		for (int i = arguments.arguments.length; i < instancedHeader.parameters.length; i++) {
			FunctionParameter parameter = instancedHeader.parameters[i];
			if (parameter.defaultValue == null && !parameter.variadic) {
				validator.logError(ValidationLogEntry.Code.INVALID_CALL_ARGUMENT, position, parameter.name == null ? "missing call argument" : "missing call argument for " + parameter.name);
			}
		}
	}
}
