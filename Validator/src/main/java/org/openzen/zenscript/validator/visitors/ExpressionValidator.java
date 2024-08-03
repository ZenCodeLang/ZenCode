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
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
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
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;

import java.util.*;

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
					expression.position,
					CompileErrors.invalidOperand("left hand side operand of && must be a bool"));
		}
		if (expression.right.type != BasicTypeID.BOOL) {
			validator.logError(
					expression.position,
					CompileErrors.invalidOperand("right hand side operand of && must be a bool"));
		}
		return null;
	}

	@Override
	public Void visitArray(ArrayExpression expression) {
		for (Expression element : expression.expressions) {
			if (!element.type.equals(expression.arrayType.elementType)) {
				validator.logError(
						expression.position,
						CompileErrors.invalidOperand("array element expression type " + element.type + " doesn't match array type " + expression.arrayType.elementType));
			}
			element.accept(this);
		}
		return null;
	}

	@Override
	public Void visitCompare(CompareExpression expression) {
		if (expression.operator.getHeader().parameters.length == 0)
			validator.logError(expression.position, CompileErrors.invalidOperand("comparison operator has no parameters!"));
		else if (!expression.right.type.equals(expression.operator.getHeader().parameters[0].type))
			validator.logError(expression.position, CompileErrors.invalidOperand("comparison has invalid right type!"));

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

		FunctionHeader instancedHeader = expression.method.hasWideningConversions() ? expression.method.method.getHeader() : expression.method.getHeader();
		checkCallArguments(expression.position, expression.method.method.getHeader(), instancedHeader, expression.arguments);
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
				validator.logError(expression.position, CompileErrors.notInstanceCallableMethod(id));
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
				validator.logError(expression.position, CompileErrors.notStaticCallableMethod(id));
				return null;
		}
	}

	@Override
	public Void visitCallSuper(CallSuperExpression expression) {
		expression.target.accept(this);
		checkMemberAccess(expression.position, expression.method);

		FunctionHeader instancedHeader = expression.method.hasWideningConversions() ? expression.method.method.getHeader() : expression.method.getHeader();
		checkCallArguments(expression.position, expression.method.method.getHeader(), instancedHeader, expression.arguments);
		checkNotStatic(expression.position, expression.method);

		MethodID id = expression.method.getID();
		switch (id.getKind()) {
			case INSTANCEMETHOD:
			case OPERATOR:
			case GETTER:
			case SETTER:
				return null;
			default:
				validator.logError(expression.position, CompileErrors.notInstanceCallableMethod(id));
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
			validator.logError(expression.position, CompileErrors.invalidOperand("target of a null check is not optional"));
		}
		return null;
	}

	@Override
	public Void visitCoalesce(CoalesceExpression expression) {
		expression.left.accept(this);
		expression.right.accept(this);
		if (!expression.left.type.isOptional()) {
			validator.logError(expression.position, CompileErrors.invalidOperand("target of a null coalesce is not optional"));
		}
		return null;
	}

	@Override
	public Void visitConditional(ConditionalExpression expression) {
		expression.condition.accept(this);
		expression.ifThen.accept(this);
		expression.ifElse.accept(this);
		if (expression.condition.type != BasicTypeID.BOOL) {
			validator.logError(expression.position, CompileErrors.invalidOperand("conditional expression condition must be a bool"));
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
			validator.logError(expression.position, CompileErrors.constructorForwardOutsideConstructor());
		}
		if (!scope.isFirstStatement()) {
			validator.logError(expression.position, CompileErrors.constructorForwardMustBeFirstStatement());
		}
		scope.markConstructorForwarded();
		checkCallArguments(expression.position, expression.constructor.getHeader(), expression.constructor.getHeader(), expression.arguments);
		return null;
	}

	@Override
	public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		checkMemberAccess(expression.position, expression.constructor);

		if (!scope.isConstructor()) {
			validator.logError(expression.position, CompileErrors.constructorForwardOutsideConstructor());
		}
		if (!scope.isFirstStatement()) {
			validator.logError(expression.position, CompileErrors.constructorForwardMustBeFirstStatement());
		}
		scope.markConstructorForwarded();
		checkCallArguments(expression.position, expression.constructor.getHeader(), expression.constructor.getHeader(), expression.arguments);
		return null;
	}

	@Override
	public Void visitEnumConstant(EnumConstantExpression expression) {
		if (!scope.isEnumConstantInitialized(expression.value)) {
			validator.logError(expression.position, CompileErrors.enumConstantNotYetInitialized(expression.value.name));
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
					expression.position,
					CompileErrors.fieldNotYetInitialized(expression.field.getName()));
		}
		return null;
	}

	@Override
	public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		return null;
	}

	@Override
	public Void visitGetLocalVariable(GetLocalVariableExpression expression) {
		if (!scope.isLocalVariableInitialized(expression.variable.id)) {
			validator.logError(expression.position, CompileErrors.localVariableNotYetInitialized(expression.variable.name));
		}
		return null;
	}

	@Override
	public Void visitGetMatchingVariantField(GetMatchingVariantField expression) {
		if (expression.index >= expression.value.parameters.length) {
			validator.logError(expression.position, CompileErrors.invalidMatchingVariantField(expression.index));
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
		validator.logError(expression.position, expression.error);
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
				validator.logError(key.position, CompileErrors.invalidOperand("Key type " + key.type + " must match the associative array key type " + type.keyType));
			}
			if (!value.type.equals(type.valueType)) {
				validator.logError(key.position, CompileErrors.invalidOperand("Value type " + value.type + " must match the associative array value type " + type.valueType));
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
						validator.logError(expression.position, CompileErrors.duplicateDefaultMatchCase());

					hasDefault = true;
				} else if (case_.key instanceof VariantOptionSwitchValue) {
					VariantDefinition.Option option = ((VariantOptionSwitchValue) case_.key).option.getOption();
					if (options.contains(option))
						validator.logError(expression.position, CompileErrors.duplicateMatchCase(option.name));

					options.add(option);
				} else {
					validator.logError(expression.position, CompileErrors.invalidVariantMatchCase());
				}
			}

			if (!hasDefault) {
				VariantDefinition variant = (VariantDefinition) ((DefinitionTypeID) expression.value.type).definition;
				List<String> missingOptions = new ArrayList<>();
				for (VariantDefinition.Option option : variant.options) {
					if (!options.contains(option))
						missingOptions.add(option.name);
				}
				if (!missingOptions.isEmpty()) {
					validator.logError(expression.position, CompileErrors.incompleteMatch(missingOptions));
				}
			}
		} else if (expression.type.isEnum()) {
			Set<EnumConstantMember> options = new HashSet<>();
			for (MatchExpression.Case case_ : expression.cases) {
				if (case_.key == null) {
					if (hasDefault)
						validator.logError(expression.position, CompileErrors.duplicateDefaultMatchCase());

					hasDefault = true;
				} else if (case_.key instanceof EnumConstantSwitchValue) {
					EnumConstantMember option = ((EnumConstantSwitchValue) case_.key).constant;
					if (options.contains(option))
						validator.logError(expression.position, CompileErrors.duplicateMatchCase(option.name));

					options.add(option);
				} else {
					validator.logError(expression.position, CompileErrors.invalidEnumMatchCase());
				}
			}

			if (!hasDefault) {
				EnumDefinition enum_ = (EnumDefinition) ((DefinitionTypeID) expression.value.type).definition;
				List<String> missingOptions = new ArrayList<>();
				for (EnumConstantMember option : enum_.enumConstants) {
					if (!options.contains(option))
						missingOptions.add(option.name);
				}
				if (!missingOptions.isEmpty()) {
					validator.logError(expression.position, CompileErrors.incompleteMatch(missingOptions));
				}
			}
		} else {
			for (MatchExpression.Case case_ : expression.cases) {
				if (case_.key == null) {
					if (hasDefault)
						validator.logError(expression.position, CompileErrors.duplicateDefaultMatchCase());

					hasDefault = true;
				}
			}

			if (!hasDefault)
				validator.logError(expression.position, CompileErrors.incompleteMatchBecauseDefaultRequired());
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
			validator.logError(expression.position, CompileErrors.invalidOperand("Left hand side of || expression is not a bool"));
		if (expression.right.type != BasicTypeID.BOOL)
			validator.logError(expression.position, CompileErrors.invalidOperand("Right hand side of || expression is not a bool"));

		return null;
	}

	@Override
	public Void visitPanic(PanicExpression expression) {
		expression.value.accept(this);
		if (expression.value.type != BasicTypeID.STRING)
			validator.logError(expression.position, CompileErrors.panicArgumentMustBeString());

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
			validator.logError(expression.position, CompileErrors.invalidOperand("From operand must be a " + rangeType.baseType));
		}
		if (!expression.to.type.equals(rangeType.baseType)) {
			validator.logError(expression.position, CompileErrors.invalidOperand("To operand must be a " + rangeType.baseType));
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
		checkCorrectType(expression.position, expression.field.getType(), expression.value.type);
		if (expression.field.getModifiers().isFinal()) {
			if (!(expression.target instanceof ThisExpression && scope.isConstructor())) {
				validator.logError(expression.position, CompileErrors.cannotSetFinalField(expression.field.getName()));
			}
		}
		return null;
	}

	@Override
	public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		expression.value.accept(this);
		checkCorrectType(expression.position, expression.parameter.type, expression.value.type);
		return null;
	}

	@Override
	public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
		expression.value.accept(this);
		checkCorrectType(expression.position, expression.variable.type, expression.value.type);
		if (expression.variable.isFinal) {
			validator.logError(expression.position, CompileErrors.cannotSetFinalVariable(expression.variable.name));
		}
		return null;
	}

	@Override
	public Void visitSetStaticField(SetStaticFieldExpression expression) {
		checkFieldAccess(expression.position, expression.field.field);
		checkStatic(expression.position, expression.field);

		expression.value.accept(this);
		checkCorrectType(expression.position, expression.field.getType(), expression.value.type);
		if (expression.field.getModifiers().isFinal()) {
			if (!scope.isStaticInitializer() || expression.field.field.getDefiningType() != scope.getDefinition()) {
				validator.logError(
						expression.position,
						CompileErrors.cannotSetFinalField(expression.field.getName()));
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
			validator.logError(expression.position, CompileErrors.noThisInScope());
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
			validator.logError(expression.position, CompileErrors.invalidCallArgument("Invalid number of variant arguments for variant element " + expression.option.getName()));
		}
		for (int i = 0; i < expression.getNumberOfArguments(); i++) {
			if (!expression.arguments[i].type.equals(expression.option.types[i])) {
				validator.logError(
						expression.position,
						CompileErrors.invalidCallArgument("Invalid variant argument for argument " + i + ": " + expression.arguments[i].type + " given but " + expression.option.types[i] + " expected"));
			}
		}
		return null;
	}

	@Override
	public Void visitWrapOptional(WrapOptionalExpression expression) {
		expression.value.accept(this);
		if (expression.value.type.isOptional()) {
			validator.logError(expression.position, CompileErrors.invalidOperand("expression value is already optional"));
		}
		return null;
	}

	private void checkCorrectType(CodePosition position, TypeID expected, TypeID actual) {
		if (actual == BasicTypeID.INVALID)
			return; // in this case the underlying error will already by reported elsewhere

		if(expected.isOptional() && actual == BasicTypeID.NULL) {
			return;
		}

		if (!expected.equals(actual)) {
			validator.logError(position, CompileErrors.typeMismatch(expected, actual));
		}
	}

	private void checkMemberAccess(CodePosition position, MethodInstance member) {
		if (!hasAccess(member.getModifiers(), member.method.getDefiningType())) {
			validator.logError(position, CompileErrors.noAccess("no access to " + member.getID()));
		}
	}

	private void checkFieldAccess(CodePosition position, FieldSymbol field) {
		if (!hasAccess(field.getModifiers(), field.getDefiningType()))
			validator.logError(position, CompileErrors.noAccess("no field access to " + field.getName()));
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
			validator.logError(position, CompileErrors.memberMustBeStatic());
	}

	private void checkStatic(CodePosition position, FieldInstance member) {
		if (!member.getModifiers().isStatic())
			validator.logError( position, CompileErrors.memberMustBeStatic());
	}

	private void checkNotStatic(CodePosition position, FieldInstance member) {
		if (member.getModifiers().isStatic())
			validator.logError(position, CompileErrors.memberMustNotBeStatic());
	}

	private void checkNotStatic(CodePosition position, MethodInstance member) {
		if (member.getModifiers().isStatic())
			validator.logError(position, CompileErrors.memberMustNotBeStatic());
	}

	private void checkCallArguments(CodePosition position, FunctionHeader originalHeader, FunctionHeader instancedHeader, CallArguments arguments) {
		ValidationUtils.validateTypeArguments(validator, position, originalHeader.typeParameters, arguments.typeArguments);
		if (arguments.typeArguments.length != 0) {
			instancedHeader = instancedHeader.instanceForCall(arguments);
		}

		boolean isVariadic = instancedHeader.isVariadicCall(arguments);
		for (int i = 0; i < arguments.arguments.length; i++) {
			Expression argument = arguments.arguments[i];
			argument.accept(this);

			if (i >= instancedHeader.parameters.length) {
				Optional<FunctionParameter> variadic = instancedHeader.getVariadicParameter();
				if (!variadic.isPresent()) {
					validator.logError(position, CompileErrors.invalidCallArgument("too many call arguments"));
					break;
				} else {
					Optional<ArrayTypeID> maybeArray = variadic.get().type.asArray();
					if (maybeArray.isPresent()) {
						TypeID elementType = maybeArray.get().elementType;
						checkCorrectType(position, elementType, argument.type);
					} else {
						validator.logError(position, CompileErrors.invalidCallArgument("variadic parameter is not an array"));
					}
				}
			}

			FunctionParameter parameter = instancedHeader.getParameter(isVariadic, i);
			checkCorrectType(position, parameter.type, arguments.arguments[i].type);
		}

		for (int i = arguments.arguments.length; i < instancedHeader.parameters.length; i++) {
			FunctionParameter parameter = instancedHeader.parameters[i];
			if (parameter.defaultValue == null && !parameter.variadic) {
				validator.logError(position, CompileErrors.invalidCallArgument("missing call argument for " + parameter.name));
			}
		}
	}
}
