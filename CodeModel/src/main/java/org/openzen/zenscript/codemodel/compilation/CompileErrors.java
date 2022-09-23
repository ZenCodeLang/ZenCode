package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CompileError;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
import java.util.stream.Collectors;

public class CompileErrors {
	private CompileErrors() {}

	public static CompileError noIntersectionBetweenTypes(TypeID a, TypeID b) {
		return new CompileError(CompileExceptionCode.TYPE_CANNOT_UNITE, "Cannot find intersection between " + a + " and " + b);
	}

	public static CompileError ambiguousType(List<TypeID> candidates) {
		String possibleTypes = candidates.stream().map(Object::toString).collect(Collectors.joining(", "));
		return new CompileError(CompileExceptionCode.INFERENCE_AMBIGUOUS, "Type inference ambiguity, possible types: " + possibleTypes);
	}

	public static CompileError noMemberInType(TypeID type, String name) {
		return new CompileError(CompileExceptionCode.NO_SUCH_MEMBER, "No member " + name + " in type " + type);
	}

	public static CompileError noGetterInType(TypeID type, String name) {
		return new CompileError(CompileExceptionCode.NO_SUCH_MEMBER, "No getter " + name + " in type " + type);
	}

	public static CompileError noSetterInType(TypeID type, String name) {
		return new CompileError(CompileExceptionCode.NO_SUCH_MEMBER, "No setter " + name + " in type " + type);
	}

	public static CompileError noFieldInType(TypeID type, String name) {
		return new CompileError(CompileExceptionCode.NO_SUCH_MEMBER, "No field " + name + " in type " + type);
	}

	public static CompileError noOperatorInType(TypeID type, OperatorType operator) {
		return new CompileError(CompileExceptionCode.NO_SUCH_MEMBER, "No operator " + operator + " in type " + type);
	}

	public static CompileError noThisInScope() {
		return new CompileError(CompileExceptionCode.USING_THIS_OUTSIDE_TYPE, "Not in an instance method; cannot use this");
	}

	public static CompileError localTypeNoSuper() {
		return new CompileError(CompileExceptionCode.SUPER_CALL_NO_SUPERCLASS, "Local class has no supertype");
	}

	public static CompileError invalidSwitchCaseExpression() {
		return new CompileError(CompileExceptionCode.INVALID_SWITCH_CASE, "Invalid expression for switch case");
	}

	public static CompileError cannotUseTypeAsValue() {
		return new CompileError(CompileExceptionCode.TYPE_NOT_VALUE, "Type cannot be used as value");
	}

	public static CompileError associativeKeyCannotHaveTypeParameters() {
		return new CompileError(CompileExceptionCode.INVALID_ASSOC_KEY,
				"Associative array key cannot have type parameters");
	}

	public static CompileError noMatchValuesForInference() {
		return new CompileError(CompileExceptionCode.CANNOT_INFER_RETURN_TYPE, "Cannot infer value from an empty match expression");
	}

	public static CompileError tryConvertWithoutResult() {
		return new CompileError(CompileExceptionCode.TRY_CONVERT_WITHOUT_RESULT, "try? expression requires result return type");
	}

	public static CompileError tryConvertRequiresThrow() {
		return new CompileError(CompileExceptionCode.TRY_CONVERT_WITHOUT_THROW, "try? expression is only valid on an expression which throws");
	}

	public static CompileError noDollarHere() {
		return new CompileError(CompileExceptionCode.NO_DOLLAR_HERE, "No dollar expression available in this context");
	}

	public static CompileError stringInsteadOfChar() {
		return new CompileError(CompileExceptionCode.INVALID_SWITCH_CASE, "char value expected but string given");
	}

	public static CompileError stringForNonStringSwitchValue(TypeID type) {
		return new CompileError(CompileExceptionCode.INVALID_SWITCH_CASE, "canot use string as " + type + " switch value");
	}

	public static CompileError typeArgumentsNotAllowedHere() {
		return new CompileError(CompileExceptionCode.TYPE_ARGUMENTS_NOT_ALLOWED, "Type arguments not allowed here");
	}

	public static CompileError invalidLValue() {
		return new CompileError(CompileExceptionCode.CANNOT_ASSIGN, "Invalid lvalue");
	}

	public static CompileError invalidArrayType(TypeID type) {
		return new CompileError(CompileExceptionCode.INVALID_ARRAY_TYPE, "Cannot compile an array as " + type);
	}

	public static CompileError noSuchVariable(ExpressionCompiler compiler, String name) {
		StringBuilder builder = new StringBuilder("No such symbol: " + name);
		List<String> possibleImports = compiler.findCandidateImports(name);
		if (!possibleImports.isEmpty()){
			builder.append("\nPossible imports:");
			possibleImports.forEach(n -> builder.append("\n").append(n));
		}

		return new CompileError(CompileExceptionCode.UNDEFINED_VARIABLE, builder.toString());
	}

	public static CompileError tryRethrowRequiresResult() {
		return new CompileError(CompileExceptionCode.TRY_RETHROW_NO_THROW, "Cannot rethrow: no throws or try/catch and no result return type");
	}

	public static CompileError cannotThrowHere() {
		return new CompileError(CompileExceptionCode.CANNOT_THROW_HERE, "Cannot throw here");
	}

	public static CompileError cannotInferEmptyArray() {
		return new CompileError(CompileExceptionCode.UNTYPED_EMPTY_ARRAY, "Cannot infer type of empty array");
	}

	public static CompileError invalidNumberOfArguments(int actual, int expected) {
		return new CompileError(CompileExceptionCode.INVALID_ARGUMENTS, "Invalid number of arguments, " + expected + " expected but " + actual + " given");
	}

	public static CompileError cannotInfer() {
		return new CompileError(CompileExceptionCode.CANNOT_INFER_RETURN_TYPE, "Could not infer return type");
	}

	public static CompileError cannotCall() {
		return new CompileError(CompileExceptionCode.CANNOT_CALL, "Not a callable expression");
	}

	public static CompileError invalidFloatSuffix(String suffix) {
		return new CompileError(CompileExceptionCode.INVALID_SUFFIX, "Invalid number literal suffix: " + suffix);
	}

	public static CompileError cannotCompileFloatLiteralAs(TypeID type) {
		return new CompileError(CompileExceptionCode.INVALID_CAST, "Cannot compile float literal as " + type);
	}

	public static CompileError cannotCompileFloatLiteralAs(TypeID type, String suffix) {
		return new CompileError(CompileExceptionCode.INVALID_CAST, "Cannot compile float literal as " + type + " using suffix " + suffix);
	}

	public static CompileError cannotCast(TypeID fromType, TypeID toType, boolean explicit) {
		if (explicit) {
			return new CompileError(CompileExceptionCode.INVALID_CAST, "Cannot cast " + fromType + " to " + toType + ", even explicitly");
		} else {
			return new CompileError(CompileExceptionCode.INVALID_CAST, "Cannot implicitly cast " + fromType + " to " + toType);
		}
	}

	public static CompileError bracketMultipleExpressions() {
		return new CompileError(CompileExceptionCode.BRACKET_MULTIPLE_EXPRESSIONS, "Bracket expression may have only one expression");
	}

	public static CompileError invalidIntSuffix(String suffix) {
		return new CompileError(CompileExceptionCode.INVALID_SUFFIX, "Invalid integer literal suffix: " + suffix);
	}

	public static CompileError cannotInferNull() {
		return new CompileError(CompileExceptionCode.CANNOT_INFER_RETURN_TYPE, "Cannot infer the type of null");
	}

	public static CompileError invalidRangeType(TypeID type) {
		return new CompileError(CompileExceptionCode.INVALID_TYPE, "Cannot construct range from type " + type);
	}

	public static CompileError invalidCharLiteral() {
		return new CompileError(CompileExceptionCode.INVALID_CHAR_LITERAL, "char value expected but string given");
	}

	public static CompileError superNotExpression() {
		return new CompileError(CompileExceptionCode.NOT_AN_EXPRESSION, "super is not a valid expression");
	}

	public static CompileError noContextMemberInType(TypeID type, String name) {
		return new CompileError(CompileExceptionCode.NO_SUCH_MEMBER, "No enum or variant member named " + name + " in type " + type);
	}

	public static CompileError genericMapConstructedEmpty() {
		return new CompileError(CompileExceptionCode.GENERIC_MAP_CONSTRUCTED_EMPTY, "Generic map construction must be empty");
	}

	public static CompileError invalidMapType(TypeID type) {
		return new CompileError(CompileExceptionCode.INVALID_TYPE, "Cannot construct map from type " + type);
	}

	public static CompileError invalidMapKey() {
		return new CompileError(CompileExceptionCode.INVALID_ASSOC_KEY, "Invalid map key");
	}

	public static CompileError missingParameter(String name) {
		return new CompileError(CompileExceptionCode.MISSING_PARAMETER, "Parameter " + name + " missing in method invocation");
	}

	public static CompileError unknownParameter(String name) {
		return new CompileError(CompileExceptionCode.NO_SUCH_PARAMETER, "No parameter with the name " + name + " exists");
	}

	public static CompileError cannotInferEmptyMap() {
		return new CompileError(CompileExceptionCode.UNTYPED_EMPTY_MAP, "Cannot infer type of empty map");
	}

	public static CompileError invalidPostfix() {
		return new CompileError(CompileExceptionCode.INVALID_POSTFIX, "Invalid postfix expression");
	}

	public static CompileError breakWithoutLoop(String name) {
		return new CompileError(CompileExceptionCode.BREAK_OUTSIDE_LOOP, name == null ? "Not in a loop" : "No such loop: " + name);
	}

	public static CompileError continueOutsideLoop(String name) {
		return new CompileError(CompileExceptionCode.CONTINUE_OUTSIDE_LOOP, name == null ? "Not in a loop" : "No such loop: " + name);
	}

	public static CompileError noSuchIterator(TypeID listType, int variables) {
		return new CompileError(CompileExceptionCode.NO_SUCH_ITERATOR, listType + " doesn't have an iterator with " + variables + " variables");
	}

	public static CompileError returnOutsideFunction() {
		return new CompileError(CompileExceptionCode.RETURN_OUTSIDE_FUNCTION, "Cannot return a value outside a function");
	}

	public static CompileError varWithoutTypeOrInitializer() {
		return new CompileError(CompileExceptionCode.VAR_WITHOUT_TYPE_OR_INITIALIZER, "Local variables must have either a type or an initializer");
	}

	public static CompileError rangeTypeFromToDifferent() {
		return new CompileError(CompileExceptionCode.NO_SUCH_TYPE, "from and to in a range must be the same type");
	}

	public static CompileError invalidTypeArguments() {
		return new CompileError(CompileExceptionCode.INVALID_TYPE_ARGUMENTS, "Invalid number of type arguments");
	}

    public static CompileError noInnerType(TypeID type, String name) {
		return new CompileError(CompileExceptionCode.NO_SUCH_TYPE, "No such inner type in " + type + ": " + name);
    }

	public static CompileError notAStringConstant() {
		return new CompileError(CompileExceptionCode.NOT_A_CONSTANT, "Not a string constant");
	}

    public static CompileError ambiguousCall(List<FunctionHeader> candidates) {
		StringBuilder message = new StringBuilder("Ambiguous call; multiple headers match:");
		for (FunctionHeader header : candidates) {
			message.append("\n").append(header.toString());
		}
		return new CompileError(CompileExceptionCode.CALL_AMBIGUOUS, message.toString());
    }

	public static CompileError noMethodMatched(List<FunctionHeader> candidates) {
		StringBuilder message = new StringBuilder("Method invocation invalid, none of these overloads match:");
		for (FunctionHeader header : candidates) {
			message.append("\n").append(header.toString());
			// for (FunctionHeader candidate : candidateFunctions)
			//   explanation.append(candidate.explainWhyIncompatible(scope, arguments)).append("\n");
		}
		return new CompileError(CompileExceptionCode.CALL_NO_VALID_METHOD, message.toString());
	}

	public static CompileError annotationNotFound(String type) {
		return new CompileError(CompileExceptionCode.UNKNOWN_ANNOTATION, "Unknown annotation type: " + type);
	}

	public static CompileError duplicateGlobal(String name) {
		return new CompileError(CompileExceptionCode.DUPLICATE_GLOBAL, "Duplicate global: " + name);
	}

	public static CompileError variantValueWithoutArguments() {
		return new CompileError(CompileExceptionCode.VARIANT_OPTION_NOT_AN_EXPRESSION, "Variant options need parameters");
	}

	public static CompileError noSuchModule(String name) {
		return new CompileError(CompileExceptionCode.NO_SUCH_MODULE, "Module not found: " + name);
	}

	public static CompileError overrideWithoutBase() {
		return new CompileError(CompileExceptionCode.OVERRIDE_WITHOUT_BASE, "Override specified without base type");
	}

	public static CompileError overriddenMethodNotFound(MethodID method, FunctionHeader header) {
		return new CompileError(CompileExceptionCode.OVERRIDEN_METHOD_NOT_FOUND, "Could not find the base method for " + method + header);
	}

	public static CompileError fieldWithoutType() {
		return new CompileError(CompileExceptionCode.PRECOMPILE_FAILED, "Could not infer type since no initializer is given");
	}

	public static CompileError ambiguousExpansionCall(List<MethodInstance> methods) {
		return new CompileError(CompileExceptionCode.AMBIGUOUS_EXPANSION_CALL, "Ambigious expansion call (" + methods.size() + " candidates)");
	}

	public static CompileError deserializationError(String message) {
		return new CompileError(CompileExceptionCode.DESERIALIZATION_ERROR, message);
	}

	public static CompileError notAnExpression(String message) {
		return new CompileError(CompileExceptionCode.NOT_AN_EXPRESSION, message);
	}

	public static CompileError duplicateFieldName(String name) {
		return new CompileError(CompileExceptionCode.DUPLICATE_FIELD_NAME, "Duplicate field name: " + name);
	}

	public static CompileError constValueInvalidType(String name, TypeID expected, TypeID actual) {
		return new CompileError(CompileExceptionCode.CONST_INVALID_INITIALIZER, "Invalid initializer for const " + name + ": " + expected + " expected but " + actual + " given");
	}

	public static CompileError duplicateConstructor(FunctionHeader existing) {
		return new CompileError(CompileExceptionCode.DUPLICATE_CONSTRUCTOR, "Duplicate constructor, conflicts with this" + existing);
	}

	public static CompileError constructorWithoutBody() {
		return new CompileError(CompileExceptionCode.METHOD_BODY_REQUIRED, "Constructors must have a method body");
	}

	public static CompileError constructorNotForwarded() {
		return new CompileError(CompileExceptionCode.CONSTRUCTOR_FORWARD_MISSING, "Constructor not forwarded to base type");
	}

	public static CompileError duplicateEnumValue(String name) {
		return new CompileError(CompileExceptionCode.DUPLICATE_ENUM_VALUE, "Duplicate enum value: " + name);
	}

	public static CompileError duplicateDestructor() {
		return new CompileError(CompileExceptionCode.DUPLICATE_DESTRUCTOR, "Type already has a destructor");
	}

	public static CompileError destructorCannotThrow() {
		return new CompileError(CompileExceptionCode.DESTRUCTOR_CANNOT_THROW, "Destructor cannot throw exceptions");
	}

	public static CompileError cannotNestImplementations() {
		return new CompileError(CompileExceptionCode.CANNOT_NEST_IMPLEMENTATIONS, "Implementations cannot be nested");
	}

	public static CompileError duplicateImplementation(TypeID type) {
		return new CompileError(CompileExceptionCode.DUPLICATE_IMPLEMENTATION, "Type is already implemented: " + type);
	}

	public static CompileError invalidImplementedType(TypeID type) {
		return new CompileError(CompileExceptionCode.INVALID_IMPLEMENTED_TYPE, "Cannot implement " + type + " since it is not an interface");
	}

	public static CompileError duplicateMember(String name) {
		return new CompileError(CompileExceptionCode.DUPLICATE_MEMBER, "Duplicate member: " + name);
	}

	public static CompileError staticInitializerCannotThrow() {
		return new CompileError(CompileExceptionCode.CANNOT_THROW_HERE, "Static initializer cannot throw exceptions");
	}

	public static CompileError cannotThrowWithoutThrows() {
		return new CompileError(CompileExceptionCode.CANNOT_THROW_HERE, "Method is throwing but doesn't declare thrown type in method header");
	}

	public static CompileError invalidThrownType(TypeID expected, TypeID actual) {
		return new CompileError(CompileExceptionCode.INVALID_THROWN_TYPE, "Thrown type is wrong: " + expected + " expected but " + actual + " thrown");
	}

	public static CompileError invalidOperand(String message) {
		return new CompileError(CompileExceptionCode.INVALID_OPERAND_TYPE, message);
	}

	public static CompileError notInstanceCallableMethod(MethodID id) {
		return new CompileError(CompileExceptionCode.CANNOT_CALL, "Cannot call this method on a value: " + id + "(" + id.getKind() + ")");
	}

	public static CompileError notStaticCallableMethod(MethodID id) {
		return new CompileError(CompileExceptionCode.CANNOT_CALL, "Cannot call this method statically: " + id + "(" + id.getKind() + ")");
	}

	public static CompileError constructorForwardOutsideConstructor() {
		return new CompileError(CompileExceptionCode.CONSTRUCTOR_FORWARD_OUTSIDE_CONSTRUCTOR, "Can only forward constructors inside constructors");
	}

	public static CompileError constructorForwardMustBeFirstStatement() {
		return new CompileError(CompileExceptionCode.CONSTRUCTOR_FORWARD_NOT_FIRST_STATEMENT, "Constructor forward must be first statement in the constructor");
	}

	public static CompileError enumConstantNotYetInitialized(String name) {
		return new CompileError(CompileExceptionCode.ENUM_CONSTANT_NOT_YET_INITIALIZED, "Using an enum constant that is not yet initialized: " + name);
	}

	public static CompileError fieldNotYetInitialized(String name) {
		return new CompileError(CompileExceptionCode.FIELD_NOT_YET_INITIALIZED, "Field " + name + " is not yet initialized");
	}

	public static CompileError localVariableNotYetInitialized(String name) {
		return new CompileError(CompileExceptionCode.LOCAL_VARIABLE_NOT_YET_INITIALIZED,  "Local variable not yet initialized: " + name);
	}

	public static CompileError invalidMatchingVariantField(int index) {
		return new CompileError(CompileExceptionCode.INVALID_MATCHING_VARIANT_FIELD, "Invalid matching field field (" + index + ")");
	}

	public static CompileError duplicateDefaultMatchCase() {
		return new CompileError(CompileExceptionCode.DUPLICATE_DEFAULT_MATCH_CASE, "Duplicate default in match");
	}

	public static CompileError duplicateMatchCase(String name) {
		return new CompileError(CompileExceptionCode.DUPLICATE_MATCH_CASE, "Duplicate case in match: " + name);
	}

	public static CompileError invalidVariantMatchCase() {
		return new CompileError(CompileExceptionCode.INVALID_MATCH_CASE, "Invalid match case: must be a variant option or default");
	}

	public static CompileError invalidEnumMatchCase() {
		return new CompileError(CompileExceptionCode.INVALID_MATCH_CASE, "Invalid match case: must be a enum constant or default");
	}

	public static CompileError incompleteMatch(List<String> missingOptions) {
		return new CompileError(
				CompileExceptionCode.INCOMPLETE_MATCH,
				"Incomplete match: missing cases " + String.join(", ", missingOptions));
	}

	public static CompileError incompleteMatchBecauseDefaultRequired() {
		return new CompileError(CompileExceptionCode.INCOMPLETE_MATCH, "Incomplete match: must have a default option");
	}

	public static CompileError panicArgumentMustBeString() {
		return new CompileError(CompileExceptionCode.PANIC_ARGUMENT_NO_STRING, "Panic argument must be a string");
	}

	public static CompileError cannotSetFinalField(String name) {
		return new CompileError(CompileExceptionCode.CANNOT_SET_FINAL_FIELD,  "Cannot update field " + name + " since it is final");
	}

	public static CompileError typeMismatch(TypeID expected, TypeID actual) {
		return new CompileError(CompileExceptionCode.TYPE_MISMATCH, "Provided a " + actual + " where a " + expected + " was expected");
	}

	public static CompileError cannotSetFinalVariable(String name) {
		return new CompileError(CompileExceptionCode.CANNOT_SET_FINAL_VARIABLE, "Cannot update " + name + " since it is a final variable");
	}

	public static CompileError invalidCallArgument(String message) {
		return new CompileError(CompileExceptionCode.INVALID_CALL_ARGUMENT, message);
	}

	public static CompileError noAccess(String message) {
		return new CompileError(CompileExceptionCode.NO_ACCESS, message);
	}

	public static CompileError memberMustBeStatic() {
		return new CompileError(CompileExceptionCode.MEMBER_NOT_STATIC, "Static member expected");
	}

	public static CompileError memberMustNotBeStatic() {
		return new CompileError(CompileExceptionCode.MEMBER_IS_STATIC, "Non-static member expected");
	}

	public static CompileError returnValueInVoidFunction() {
		return new CompileError(CompileExceptionCode.RETURN_VALUE_VOID, "Return type is void; cannot return a value");
	}

	public static CompileError missingReturnValue() {
		return new CompileError(CompileExceptionCode.MISSING_RETURN_VALUE, "Missing return value");
	}

	public static CompileError tryCatchResourceWithoutInitializer() {
		return new CompileError(CompileExceptionCode.TRY_CATCH_RESOURCE_REQUIRES_INITIALIZER, "try with resource requires initializer");
	}

	public static CompileError invalidSuperclass(TypeID type) {
		return new CompileError(CompileExceptionCode.INVALID_SUPERCLASS, type + " is not a valid superclass");
	}

	public static CompileError superclassNotVirtual(TypeID type) {
		return new CompileError(CompileExceptionCode.INVALID_SUPERCLASS, "Cannot use " + type + " as superclass since it is not virtual");
	}

	public static CompileError typeNotDetermined(String display) {
		return new CompileError(CompileExceptionCode.INVALID_TYPE,  display + " could not be inferred");
	}

	public static CompileError invalidArrayDimension(int dimension) {
		return new CompileError(CompileExceptionCode.INVALID_TYPE, "Invalid array dimension: " + dimension);
	}

	public static CompileError invalidOverride(String message) {
		return new CompileError(CompileExceptionCode.INVALID_OVERRIDE, message);
	}

	public static CompileError invalidIdentifier(String identifier) {
		return new CompileError(CompileExceptionCode.INVALID_IDENTIFIER, "Invalid identifier: " + identifier);
	}

	public static CompileError duplicateParameterName(String name) {
		return new CompileError(CompileExceptionCode.DUPLICATE_PARAMETER_NAME, "Duplicate parameter name: " + name);
	}

	public static CompileError variadicParameterMustBeLast() {
		return new CompileError(CompileExceptionCode.VARIADIC_PARAMETER_MUST_BE_LAST,  "variadic parameter must be the last parameter");
	}

	public static CompileError variadicParameterMustBeArray() {
		return new CompileError(CompileExceptionCode.VARIADIC_PARAMETER_MUST_BE_ARRAY,  "variadic parameter must be an array");
	}

	public static CompileError invalidModifier(String message) {
		return new CompileError(CompileExceptionCode.INVALID_MODIFIER, message);
	}

	public static CompileError invalidNumberOfTypeArguments(int expectedTypeArguments, int actualTypeArguments) {
		return new CompileError(CompileExceptionCode.TYPE_ARGUMENTS_INVALID_NUMBER, "Invalid number of type arguments: "
				+ actualTypeArguments
				+ " arguments given but "
				+ expectedTypeArguments
				+ " expected");
	}

	public static CompileError parseError(String message) {
		return new CompileError(CompileExceptionCode.PARSE_ERROR, message);
	}
}
