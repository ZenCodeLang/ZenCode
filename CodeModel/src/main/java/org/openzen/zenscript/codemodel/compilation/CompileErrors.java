package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CompileError;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
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
		}
		return new CompileError(CompileExceptionCode.CALL_NO_VALID_METHOD, message.toString());
	}
}
