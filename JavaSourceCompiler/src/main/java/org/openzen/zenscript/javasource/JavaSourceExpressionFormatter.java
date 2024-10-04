/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.StringExpansion;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.captured.CapturedClosureExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedParameterExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedThisExpression;
import org.openzen.zenscript.codemodel.ssa.SSAValue;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.formattershared.ExpressionString;
import org.openzen.zenscript.formattershared.StatementFormattingTarget;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javasource.scope.JavaSourceStatementScope;

/**
 * @author Hoofdgebruiker
 */
public class JavaSourceExpressionFormatter implements ExpressionVisitor<ExpressionString> {
	private static final JavaClass RESULT = new JavaClass("stdlib", "Result", JavaClass.Kind.CLASS);
	private static final JavaClass RESULT_OK = new JavaClass("stdlib", "Result.Ok", JavaClass.Kind.CLASS);
	private static final JavaClass RESULT_ERROR = new JavaClass("stdlib", "Result.Error", JavaClass.Kind.CLASS);
	private static final JavaClass STANDARD_CHARSETS = new JavaClass("java.nio.charset", "StandardCharsets", JavaClass.Kind.CLASS);

	public final JavaSourceStatementScope scope;
	public final StatementFormattingTarget target;
	private final JavaContext context;

	public JavaSourceExpressionFormatter(StatementFormattingTarget target, JavaSourceStatementScope scope) {
		this.target = target;
		this.scope = scope;
		context = scope.context;
	}

	private ExpressionString getValue(Expression expression) {
		return expression.accept(this);
	}

	@Override
	public ExpressionString visitAndAnd(AndAndExpression expression) {
		return binary(expression.left, expression.right, JavaOperator.ANDAND);
	}

	@Override
	public ExpressionString visitArray(ArrayExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append("new ");
		result.append(scope.type(expression.type));
		result.append(" {");
		int index = 0;
		for (Expression element : expression.expressions) {
			if (index > 0)
				result.append(", ");

			result.append(element.accept(this).value);
			index++;
		}
		result.append("}");
		return new ExpressionString(result.toString(), JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitCompare(CompareExpression expression) {
		if (expression.operator.getBuiltin() != null)
			return visitBuiltinCompare(expression, expression.operator.getBuiltin());

		StringBuilder output = new StringBuilder();
		output.append(getValue(expression.left).value);
		output.append(".compareTo(");
		output.append(expression.right.accept(this));
		output.append(") ");
		output.append(expression.comparison.str);
		output.append("0");

		return new ExpressionString(output.toString(), JavaOperator.getComparison(expression.comparison));
	}

	@Override
	public ExpressionString visitCall(CallExpression expression) {
		JavaMethod method = context.getJavaMethod(expression.method.method);
		return method.compile(methodCompiler, expression.target, expression.arguments);
	}

	private ExpressionString compileCall(JavaNativeMethod method, Expression target, CallArguments arguments) {
		switch (method.kind) {
			case EXPANSION: {
				StringBuilder output = new StringBuilder();
				output.append(scope.type(method.cls));
				output.append('.');
				output.append(method.name);
				FormattingUtils.formatExpansionCall(output, this.target, scope, getValue(target), arguments);
				return new ExpressionString(output.toString(), JavaOperator.CALL);
			}
			case INSTANCE:
			case INTERFACE: {
				StringBuilder output = new StringBuilder();
				output.append(getValue(target).value);
				output.append('.');
				FormattingUtils.formatCall(output, this.target, scope, method.name, arguments);
				return new ExpressionString(output.toString(), JavaOperator.CALL);
			}
			default:
				throw new IllegalStateException("Invalid method call kind: " + method.kind);
		}
	}

	@Override
	public ExpressionString visitCallStatic(CallStaticExpression expression) {
		if (expression.member.getBuiltin() != null)
			return visitBuiltinCallStatic(expression, expression.member.getBuiltin());

		JavaNativeMethod method = context.getJavaMethod(expression.member);
		if (method == null)
			throw new IllegalStateException("No source method tag for " + expression.member.getCanonicalName() + "!");

		if (method.kind == JavaNativeMethod.Kind.COMPILED)
			return (ExpressionString) method.translation.translate(expression, this);

		StringBuilder result = new StringBuilder();
		result.append(scope.type(method.cls));
		result.append('.');
		FormattingUtils.formatCall(result, target, scope, method.name, expression.arguments);
		return new ExpressionString(result.toString(), JavaOperator.CALL);
	}

	@Override
	public ExpressionString visitCapturedClosure(CapturedClosureExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ExpressionString visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
		return new ExpressionString(expression.variable.name, JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitCapturedParameter(CapturedParameterExpression expression) {
		return new ExpressionString(expression.parameter.name, JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitCapturedThis(CapturedThisExpression expression) {
		return new ExpressionString(scope.isExpansion ? "self" : "this", JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitCheckNull(CheckNullExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public ExpressionString visitCoalesce(CoalesceExpression expression) {
		Expression leftDup = duplicable(expression.left);
		ExpressionString left = leftDup.accept(this);
		ExpressionString right = expression.right.accept(this);

		StringBuilder result = new StringBuilder();
		result.append(left.value).append(" == null ? ");
		result.append(left.value).append(" : ");
		result.append(right.value);
		return new ExpressionString(result.toString(), JavaOperator.TERNARY);
	}

	@Override
	public ExpressionString visitConditional(ConditionalExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.condition.accept(this));
		result.append(" ? ");
		result.append(expression.ifThen.accept(this));
		result.append(" : ");
		result.append(expression.ifElse.accept(this));
		return new ExpressionString(result.toString(), JavaOperator.TERNARY);
	}

	@Override
	public ExpressionString visitConst(ConstExpression expression) {
		if (expression.constant.member.builtin != null)
			return visitBuiltinConstant(expression, expression.constant.member.builtin);

		return new ExpressionString(
				scope.type(expression.constant.member.definition) + "." + expression.constant.member.name,
				JavaOperator.MEMBER);
	}

	@Override
	public ExpressionString visitConstantBool(ConstantBoolExpression expression) {
		return new ExpressionString(expression.value ? "true" : "false", JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantByte(ConstantByteExpression expression) {
		return new ExpressionString(Integer.toString(expression.value), JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantChar(ConstantCharExpression expression) {
		return new ExpressionString(
				StringExpansion.escape(Character.toString(expression.value), '\'', true),
				JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantDouble(ConstantDoubleExpression expression) {
		return new ExpressionString(Double.toString(expression.value), JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantFloat(ConstantFloatExpression expression) {
		return new ExpressionString(Float.toString(expression.value) + "f", JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantInt(ConstantIntExpression expression) {
		return new ExpressionString(Integer.toString(expression.value), JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantLong(ConstantLongExpression expression) {
		return new ExpressionString(Long.toString(expression.value) + "L", JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstantSByte(ConstantSByteExpression expression) {
		return new ExpressionString("(byte)" + Byte.toString(expression.value), JavaOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantShort(ConstantShortExpression expression) {
		return new ExpressionString("(short)" + Integer.toString(expression.value), JavaOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantString(ConstantStringExpression expression) {
		return new ExpressionString(
				StringExpansion.escape(expression.value, '"', true),
				JavaOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantUInt(ConstantUIntExpression expression) {
		return new ExpressionString(Integer.toString(expression.value), JavaOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantULong(ConstantULongExpression expression) {
		return new ExpressionString(Long.toString(expression.value) + "L", JavaOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantUShort(ConstantUShortExpression expression) {
		return new ExpressionString("(short)" + Integer.toString(expression.value), JavaOperator.CAST);
	}

	@Override
	public ExpressionString visitConstantUSize(ConstantUSizeExpression expression) {
		return new ExpressionString(Integer.toString((int) expression.value), JavaOperator.CAST);
	}

	@Override
	public ExpressionString visitConstructorThisCall(ConstructorThisCallExpression expression) {
		StringBuilder result = new StringBuilder();
		FormattingUtils.formatCall(result, target, scope, "this", expression.arguments);
		return new ExpressionString(result.toString(), JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		StringBuilder result = new StringBuilder();
		FormattingUtils.formatCall(result, target, scope, "super", expression.arguments);
		return new ExpressionString(result.toString(), JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitEnumConstant(EnumConstantExpression expression) {
		return new ExpressionString(scope.type(expression.type) + "." + expression.value.name, JavaOperator.MEMBER);
	}

	@Override
	public ExpressionString visitFunction(FunctionExpression expression) {
		StringBuilder result = new StringBuilder();
		FunctionHeader header = expression.header;
		if (header.parameters.length == 1) {
			result.append(header.parameters[0].name);
		} else {
			result.append("(");
			for (int i = 0; i < header.parameters.length; i++) {
				if (i > 0)
					result.append(", ");
				result.append(header.parameters[i].name);
			}
			result.append(")");
		}
		result.append(" -> ");
		expression.body.accept(new JavaSourceLambdaStatementCompiler(scope, result, false, false));
		return new ExpressionString(result.toString(), JavaOperator.LAMBDA);
	}

	@Override
	public ExpressionString visitGetField(GetFieldExpression expression) {
		StringBuilder result = new StringBuilder();
		if (!(expression.target instanceof ThisExpression && !scope.hasLocalVariable(expression.field.member.name))) {
			result.append(getValue(expression.target));
			result.append('.');
		}
		result.append(expression.field.member.name);
		return new ExpressionString(result.toString(), JavaOperator.MEMBER);
	}

	@Override
	public ExpressionString visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		return new ExpressionString(expression.parameter.name, JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitGetLocalVariable(GetLocalVariableExpression expression) {
		return new ExpressionString(expression.variable.name, JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitGetMatchingVariantField(GetMatchingVariantField expression) {
		return new ExpressionString(expression.value.parameters[expression.index], JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitGetStaticField(GetStaticFieldExpression expression) {
		JavaNativeField field = context.getJavaField(expression.field);
		if (field == null)
			throw new RuntimeException(expression.position + ": Missing field tag");

		return new ExpressionString(scope.type(field.cls) + '.' + field.name, JavaOperator.MEMBER);
	}

	@Override
	public ExpressionString visitGetter(GetterExpression expression) {
		if (expression.getter.member.builtin != null)
			return visitBuiltinGetter(expression, expression.getter.member.builtin);

		ExpressionString target = getValue(expression.target);
		if (context.hasJavaField(expression.getter)) {
			JavaNativeField field = context.getJavaField(expression.getter);
			if (target.value.equals("this") && !scope.hasLocalVariable(field.name))
				return new ExpressionString(field.name, JavaOperator.PRIMARY);

			return target.unaryPostfix(JavaOperator.MEMBER, "." + field.name);
		}

		JavaNativeMethod method = context.getJavaMethod(expression.getter);
		StringBuilder result = new StringBuilder();
		if (!target.value.equals("this") || scope.hasLocalVariable(method.name)) {
			result.append(target.value);
			result.append(".");
		}
		result.append(method.name);
		result.append("()");
		return new ExpressionString(result.toString(), JavaOperator.MEMBER);
	}

	@Override
	public ExpressionString visitGlobal(GlobalExpression expression) {
		return new ExpressionString(expression.name, JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitGlobalCall(GlobalCallExpression expression) {
		StringBuilder result = new StringBuilder();
		FormattingUtils.formatCall(result, target, scope, expression.name, expression.arguments);
		return new ExpressionString(result.toString(), JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitInterfaceCast(InterfaceCastExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public ExpressionString visitIs(IsExpression expression) {
		StringBuilder result = new StringBuilder();
		result.append(expression.value.accept(this).value);
		result.append(" instanceof ");
		result.append(scope.type(expression.type));
		return new ExpressionString(result.toString(), JavaOperator.INSTANCEOF);
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
		return new ExpressionString(result.toString(), JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitMatch(MatchExpression expression) {
		MatchExpression.SwitchedMatch switched = expression.convertToSwitch(scope.createTempVariable());

		JavaSourceStatementFormatter formatter = new JavaSourceStatementFormatter(scope);
		formatter.formatVar(target, switched.result);
		formatter.formatSwitch(target, switched.switchStatement);

		return new ExpressionString(switched.result.name, JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitNew(NewExpression expression) {
		if (expression.constructor.getBuiltin() != null) {
			try {
				return visitBuiltinConstructor(expression);
			} catch (CompileException ex) {
				throw new RuntimeException(ex.toString());
			}
		}

		JavaNativeMethod method = context.getJavaMethod(expression.constructor);
		switch (method.kind) {
			case EXPANSION: {
				StringBuilder output = new StringBuilder();
				output.append(scope.type(method.cls));
				output.append('.');
				FormattingUtils.formatCall(output, this.target, scope, method.name, expression.arguments);
				return new ExpressionString(output.toString(), JavaOperator.CALL);
			}
			case CONSTRUCTOR: {
				StringBuilder output = new StringBuilder();
				output.append("new ");
				output.append(scope.type(expression.type, method.cls));
				FormattingUtils.formatCall(output, this.target, scope, "", expression.arguments);
				return new ExpressionString(output.toString(), JavaOperator.PRIMARY);
			}
			default:
				throw new IllegalStateException("Invalid method call kind: " + method.kind);
		}
	}

	@Override
	public ExpressionString visitNull(NullExpression expression) {
		if (expression.type.withoutOptional() == BasicTypeID.USIZE)
			return new ExpressionString("-1", JavaOperator.PRIMARY); // usize? null = -1

		return new ExpressionString("null", JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitOrOr(OrOrExpression expression) {
		return binary(expression.left, expression.right, JavaOperator.OROR);
	}

	@Override
	public ExpressionString visitPanic(PanicExpression expression) {
		return new ExpressionString("throw new AssertionError(" + expression.value.accept(this).value + ")", JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitPlatformSpecific(Expression expression) {
		throw new UnsupportedOperationException("Cannot use Platform specific yet!");
	}

	@Override
	public ExpressionString visitModification(ModificationExpression expression) {
		return unaryPostfix(expression.target, expression.member.getOperator() == OperatorType.INCREMENT ? JavaOperator.INCREMENT : JavaOperator.DECREMENT);
	}

	@Override
	public ExpressionString visitRange(RangeExpression expression) {
		StringBuilder builder = new StringBuilder();
		builder.append("new ");
		builder.append(scope.type(expression.type));
		builder.append("(");
		builder.append(expression.from.accept(this));
		builder.append(", ");
		builder.append(expression.to.accept(this));
		builder.append(")");
		return new ExpressionString(builder.toString(), JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitSameObject(SameObjectExpression expression) {
		return binary(expression.left, expression.right, expression.inverted ? JavaOperator.EQUALS : JavaOperator.NOTEQUALS);
	}

	@Override
	public ExpressionString visitSetField(SetFieldExpression expression) {
		return new ExpressionString(
				expression.target.accept(this) + "." + expression.field.member.name + " = " + expression.value.accept(this).value,
				JavaOperator.ASSIGN);
	}

	@Override
	public ExpressionString visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		return new ExpressionString(
				expression.parameter.name + " = " + expression.value.accept(this).value,
				JavaOperator.ASSIGN);
	}

	@Override
	public ExpressionString visitSetLocalVariable(SetLocalVariableExpression expression) {
		return new ExpressionString(
				expression.variable.name + " = " + expression.value.accept(this).value,
				JavaOperator.ASSIGN);
	}

	@Override
	public ExpressionString visitSetStaticField(SetStaticFieldExpression expression) {
		JavaNativeField field = context.getJavaField(expression.field);
		if (field == null)
			throw new RuntimeException(expression.position + ": Missing field tag");

		if (field.cls.fullName.equals(scope.fileScope.cls.fullName) && !scope.hasLocalVariable(field.name)) {
			return new ExpressionString(
					field.name + " = " + expression.value.accept(this).value,
					JavaOperator.ASSIGN);
		} else {
			return new ExpressionString(
					scope.type(field.cls) + "." + field.name + " = " + expression.value.accept(this).value,
					JavaOperator.ASSIGN);
		}
	}

	@Override
	public ExpressionString visitSupertypeCast(SupertypeCastExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public ExpressionString visitSubtypeCast(SubtypeCastExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public ExpressionString visitThis(ThisExpression expression) {
		if (scope.isExpansion || scope.thisType == expression.type) {
			return new ExpressionString(scope.isExpansion ? "self" : "this", JavaOperator.PRIMARY);
		} else {
			return new ExpressionString(constructThisName((DefinitionTypeID) expression.type) + ".this", JavaOperator.MEMBER);
		}
	}

	private String constructThisName(DefinitionTypeID type) {
		JavaClass cls = context.getJavaClass(type.definition);
		return cls.getClassName();
	}

	@Override
	public ExpressionString visitThrow(ThrowExpression expression) {
		return new ExpressionString("throw " + expression.value.accept(this).value, JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitTryConvert(TryConvertExpression expression) {
		ExpressionString value = hoist(expression.value).accept(this);

		return new ExpressionString("try?" + value.value, value.priority);
	}

	@Override
	public ExpressionString visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
		DefinitionTypeID type = (DefinitionTypeID) expression.value.type;

		String errorType = scope.fileScope.importer.importType(RESULT_ERROR);
		String okType = scope.fileScope.importer.importType(RESULT_OK);

		ExpressionString value = hoist(expression.value).accept(this);
		target.writeLine("if (" + value.value + " instanceof " + errorType + ")");
		target.writeLine(scope.settings.indent + "throw ((" + errorType + formatTypeArguments(type.typeArguments) + ")" + value.value + ").value;");

		return value
				.unaryPrefix(JavaOperator.CAST, "(" + okType + formatTypeArguments(type.typeArguments) + ")")
				.unaryPostfix(JavaOperator.MEMBER, ".value");
	}

	@Override
	public ExpressionString visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
		DefinitionTypeID type = (DefinitionTypeID) expression.value.type;

		String errorType = scope.fileScope.importer.importType(RESULT_ERROR);
		String okType = scope.fileScope.importer.importType(RESULT_OK);

		ExpressionString value = hoist(expression.value).accept(this);
		target.writeLine("if (" + value.value + " instanceof " + errorType + ")");
		target.writeLine(scope.settings.indent + "return new " + errorType + "<>(((" + errorType + formatTypeArguments(type.typeArguments) + ")" + value.value + ").value);");

		return value
				.unaryPrefix(JavaOperator.CAST, "(" + okType + formatTypeArguments(type.typeArguments) + ")")
				.unaryPostfix(JavaOperator.MEMBER, ".value");
	}

	@Override
	public ExpressionString visitVariantValue(VariantValueExpression expression) {
		JavaVariantOption option = context.getJavaVariantOption(expression.option);

		StringBuilder result = new StringBuilder();
		result.append("new ").append(scope.type(option.variantOptionClass));
		FormattingUtils.formatCall(result, this.target, scope, "", new CallArguments(expression.arguments));
		return new ExpressionString(result.toString(), JavaOperator.PRIMARY);
	}

	@Override
	public ExpressionString visitWrapOptional(WrapOptionalExpression expression) {
		return expression.value.accept(this);
	}

	private String formatTypeArguments(TypeID[] types) {
		if (types == null || types.length == 0)
			return "";

		StringBuilder output = new StringBuilder();
		output.append("<");

		for (int i = 0; i < types.length; i++) {
			if (i > 0)
				output.append(", ");

			output.append(types[i].accept(scope.fileScope.objectTypeVisitor));
		}
		output.append(">");
		return output.toString();
	}

	public ExpressionString newArray(TypeID elementType, ExpressionString length) {
		if (elementType instanceof GenericTypeID) {
			// generic array creation
			GenericTypeID generic = (GenericTypeID) elementType;
			String array = scope.type(new JavaClass("java.lang.reflect", "Array", JavaClass.Kind.CLASS));
			return new ExpressionString("(" + generic.parameter.name + "[])(" + array + ".newInstance(typeOf" + generic.parameter.name + ", " + length.value + "))", JavaOperator.CAST);
		} else if (elementType == BasicTypeID.BYTE) {
			return new ExpressionString("new byte[" + length.value + "]", JavaOperator.NEW);
		} else if (elementType == BasicTypeID.USHORT) {
			return new ExpressionString("new short[" + length.value + "]", JavaOperator.NEW);
		} else if (elementType == BasicTypeID.UINT) {
			return new ExpressionString("new int[" + length.value + "]", JavaOperator.NEW);
		} else if (elementType == BasicTypeID.ULONG) {
			return new ExpressionString("new long[" + length.value + "]", JavaOperator.NEW);
		} else {
			return new ExpressionString("new " + scope.type(elementType) + "[" + length.value + "]", JavaOperator.NEW);
		}
	}

	public ExpressionString unaryPrefix(Expression value, JavaOperator operator) {
		return value.accept(this).unaryPrefix(operator);
	}

	public ExpressionString unaryPrefix(CallExpression invocation, JavaOperator operator) {
		return unaryPrefix(invocation.target, operator);
	}

	public ExpressionString unaryPostfix(Expression value, JavaOperator operator) {
		return value.accept(this).unaryPostfix(operator);
	}

	public ExpressionString unaryPostfix(CallExpression invocation, JavaOperator operator) {
		return unaryPostfix(invocation.target, operator);
	}

	public ExpressionString binary(Expression left, Expression right, JavaOperator operator) {
		return ExpressionString.binary(left.accept(this), right.accept(this), operator);
	}

	public ExpressionString binary(CallExpression call, JavaOperator operator) {
		return binary(call.target, call.arguments.arguments[0], operator);
	}

	public ExpressionString call(String method, CallExpression expression) {
		StringBuilder output = new StringBuilder();
		output.append(expression.target.accept(this).value);
		output.append(".");
		output.append(method).append("(");
		boolean first = true;
		if (expression.arguments.typeArguments != null) {
			for (TypeID typeArgument : expression.arguments.typeArguments) {
				if (!first)
					output.append(", ");

				if (typeArgument instanceof GenericTypeID) {
					output.append("typeOf").append(((GenericTypeID) typeArgument).parameter.name);
				} else {
					output.append(scope.type(typeArgument));
					output.append(".class");
				}
				first = false;
			}
		}

		for (Expression argument : expression.arguments.arguments) {
			if (!first)
				output.append(", ");
			output.append(argument.accept(this).value);
			first = false;
		}
		output.append(")");
		return new ExpressionString(output.toString(), JavaOperator.CALL);
	}

	public ExpressionString call(String method, Expression left, Expression right) {
		StringBuilder output = new StringBuilder();
		output.append(left.accept(this).value);
		output.append(".");
		output.append(method).append("(");
		output.append(right.accept(this).value);
		output.append(")");
		return new ExpressionString(output.toString(), JavaOperator.CALL);
	}

	public ExpressionString callStatic(String target, Expression value) {
		return new ExpressionString(target + "(" + value.accept(this) + ")", JavaOperator.CALL);
	}

	public ExpressionString callStatic(String target, ExpressionString value) {
		return new ExpressionString(target + "(" + value.value + ")", JavaOperator.CALL);
	}

	public ExpressionString callStatic(String target, CallStaticExpression expression) {
		StringBuilder output = new StringBuilder();
		output.append(target).append("(");
		int i = 0;
		for (Expression argument : expression.arguments.arguments) {
			if (i > 0)
				output.append(", ");

			output.append(argument.accept(this).value);
			i++;
		}
		output.append(")");
		return new ExpressionString(output.toString(), JavaOperator.CALL);
	}

	public ExpressionString callStatic(String target, Expression a, Expression b) {
		StringBuilder output = new StringBuilder();
		output.append(target).append("(");
		output.append(a.accept(this).value);
		output.append(", ");
		output.append(b.accept(this).value);
		output.append(")");
		return new ExpressionString(output.toString(), JavaOperator.CALL);
	}

	public ExpressionString compare(ExpressionString value, CompareType comparator) {
		return ExpressionString.binary(
				value,
				new ExpressionString("0", JavaOperator.PRIMARY),
				JavaOperator.getComparison(comparator));
	}

	public ExpressionString compare(Expression left, Expression right, CompareType comparator) {
		return binary(left, right, JavaOperator.getComparison(comparator));
	}

	public ExpressionString compare(ExpressionString left, ExpressionString right, CompareType comparator) {
		return ExpressionString.binary(left, right, JavaOperator.getComparison(comparator));
	}

	public ExpressionString callAsStatic(String target, CallExpression expression) {
		StringBuilder output = new StringBuilder();
		output.append(target).append("(");
		output.append(expression.target.accept(this).value);
		for (Expression argument : expression.arguments.arguments) {
			output.append(", ");
			output.append(argument.accept(this).value);
		}
		output.append(")");
		return new ExpressionString(output.toString(), JavaOperator.CALL);
	}

	public ExpressionString callAsStaticTruncated(String target, CallExpression expression, JavaOperator truncator) {
		StringBuilder output = new StringBuilder();
		output.append(target).append("(");
		output.append(expression.target.accept(this).value);
		for (Expression argument : expression.arguments.arguments) {
			output.append(", ");
			output.append(argument.accept(this).unaryPostfix(truncator).value);
		}
		output.append(")");
		return new ExpressionString(output.toString(), JavaOperator.CALL);
	}

	public ExpressionString callAsStatic(String target, Expression left, Expression right) {
		StringBuilder output = new StringBuilder();
		output.append(target).append("(");
		output.append(left.accept(this).value);
		output.append(", ");
		output.append(right.accept(this).value);
		output.append(")");
		return new ExpressionString(output.toString(), JavaOperator.CALL);
	}

	public ExpressionString callAsStatic(String target, GetterExpression expression) {
		StringBuilder output = new StringBuilder();
		output.append(target).append("(");
		output.append(expression.target.accept(this).value);
		output.append(")");
		return new ExpressionString(output.toString(), JavaOperator.CALL);
	}

	private ExpressionString eval(Expression expression) {
		return expression.accept(this);
	}

/*	private ExpressionString cast(CastExpression cast, String type) {
		return cast.target.accept(this).unaryPrefix(JavaOperator.CAST, "(" + type + ")");
	}*/

	private ExpressionString cast(ExpressionString value, String type) {
		return value.unaryPrefix(JavaOperator.CALL, "(" + type + ")");
	}

/*	private ExpressionString castPostfix(CastExpression cast, JavaOperator operator) {
		return eval(cast.target).unaryPostfix(operator);
	}

	private ExpressionString castImplicit(CastExpression cast, String type) {
		return cast.isImplicit ? cast.target.accept(this) : cast(cast, type);
	}*/

	public Expression duplicable(Expression expression) {
		boolean shouldHoist = expression.accept(ExpressionHoistingChecker.INSTANCE);
		if (!shouldHoist)
			return expression;

		return hoist(expression);
	}

	private Expression hoist(Expression value) {
		VariableID variable = new VariableID();
		VarStatement temp = new VarStatement(value.position, variable, scope.createTempVariable(), value.type, value, true);
		new JavaSourceStatementFormatter(scope).formatVar(target, temp);
		return new GetLocalVariableExpression(value.position, temp, new SSAValue(variable, value));
	}

	public ExpressionString hoist(ExpressionString value, String type) {
		String temp = scope.createTempVariable();
		target.writeLine(type + " " + temp + " = " + value.value + ";");
		return new ExpressionString(temp, JavaOperator.PRIMARY);
	}

	private ExpressionString visitBuiltinCall(CallExpression call, BuiltinID builtin) {
		switch (builtin) {
			case BOOL_NOT:
				return unaryPrefix(call, JavaOperator.NOT);
			case BOOL_AND:
				return binary(call, JavaOperator.AND);
			case BOOL_OR:
				return binary(call, JavaOperator.OR);
			case BOOL_XOR:
				return binary(call, JavaOperator.XOR);
			case BOOL_EQUALS:
				return binary(call, JavaOperator.EQUALS);
			case BOOL_NOTEQUALS:
				return binary(call, JavaOperator.NOTEQUALS);
			case BYTE_NOT:
				return unaryPrefix(call, JavaOperator.INVERT);
			case BYTE_ADD_BYTE:
				return binary(call, JavaOperator.ADD);
			case BYTE_SUB_BYTE:
				return binary(call, JavaOperator.SUB);
			case BYTE_MUL_BYTE:
				return binary(call, JavaOperator.MUL);
			case BYTE_DIV_BYTE:
				return callAsStaticTruncated("Integer.divideUnsigned", call, JavaOperator.AND_FF);
			case BYTE_MOD_BYTE:
				return callAsStaticTruncated("Integer.remainderUnsigned", call, JavaOperator.AND_FF);
			case BYTE_AND_BYTE:
				return binary(call, JavaOperator.AND);
			case BYTE_OR_BYTE:
				return binary(call, JavaOperator.OR);
			case BYTE_XOR_BYTE:
				return binary(call, JavaOperator.XOR);
			case BYTE_SHL:
				return binary(call, JavaOperator.SHL);
			case BYTE_SHR:
				return binary(call, JavaOperator.SHR);
			case SBYTE_NOT:
				return unaryPrefix(call, JavaOperator.INVERT);
			case SBYTE_NEG:
				return unaryPrefix(call, JavaOperator.NEG);
			case SBYTE_ADD_SBYTE:
				return binary(call, JavaOperator.ADD);
			case SBYTE_SUB_SBYTE:
				return binary(call, JavaOperator.SUB);
			case SBYTE_MUL_SBYTE:
				return binary(call, JavaOperator.MUL);
			case SBYTE_DIV_SBYTE:
				return binary(call, JavaOperator.DIV);
			case SBYTE_MOD_SBYTE:
				return binary(call, JavaOperator.MOD);
			case SBYTE_AND_SBYTE:
				return binary(call, JavaOperator.AND);
			case SBYTE_OR_SBYTE:
				return binary(call, JavaOperator.OR);
			case SBYTE_XOR_SBYTE:
				return binary(call, JavaOperator.XOR);
			case SBYTE_SHL:
				return binary(call, JavaOperator.SHL);
			case SBYTE_SHR:
				return binary(call, JavaOperator.SHR);
			case SBYTE_USHR:
				return binary(call, JavaOperator.USHR);
			case SHORT_NOT:
				return unaryPrefix(call, JavaOperator.INVERT);
			case SHORT_NEG:
				return unaryPrefix(call, JavaOperator.NEG);
			case SHORT_ADD_SHORT:
				return binary(call, JavaOperator.ADD);
			case SHORT_SUB_SHORT:
				return binary(call, JavaOperator.SUB);
			case SHORT_MUL_SHORT:
				return binary(call, JavaOperator.MUL);
			case SHORT_DIV_SHORT:
				return binary(call, JavaOperator.DIV);
			case SHORT_MOD_SHORT:
				return binary(call, JavaOperator.MOD);
			case SHORT_AND_SHORT:
				return binary(call, JavaOperator.AND);
			case SHORT_OR_SHORT:
				return binary(call, JavaOperator.OR);
			case SHORT_XOR_SHORT:
				return binary(call, JavaOperator.XOR);
			case SHORT_SHL:
				return binary(call, JavaOperator.SHL);
			case SHORT_SHR:
				return binary(call, JavaOperator.SHR);
			case USHORT_NOT:
				return unaryPrefix(call, JavaOperator.INVERT);
			case USHORT_ADD_USHORT:
				return binary(call, JavaOperator.ADD);
			case USHORT_SUB_USHORT:
				return binary(call, JavaOperator.SUB);
			case USHORT_MUL_USHORT:
				return binary(call, JavaOperator.MUL);
			case USHORT_DIV_USHORT:
				return callAsStaticTruncated("Integer.divideUnsigned", call, JavaOperator.AND_FFFF);
			case USHORT_MOD_USHORT:
				return callAsStaticTruncated("Integer.remainderUnsigned", call, JavaOperator.AND_FFFF);
			case USHORT_AND_USHORT:
				return binary(call, JavaOperator.AND);
			case USHORT_OR_USHORT:
				return binary(call, JavaOperator.OR);
			case USHORT_XOR_USHORT:
				return binary(call, JavaOperator.XOR);
			case USHORT_SHL:
				return binary(call, JavaOperator.SHL);
			case USHORT_SHR:
				return binary(call, JavaOperator.SHR);
			case INT_NOT:
				return unaryPrefix(call, JavaOperator.INVERT);
			case INT_NEG:
				return unaryPrefix(call, JavaOperator.NEG);
			case INT_ADD_INT:
				return binary(call, JavaOperator.ADD);
			case INT_SUB_INT:
				return binary(call, JavaOperator.SUB);
			case INT_MUL_INT:
				return binary(call, JavaOperator.MUL);
			case INT_DIV_INT:
				return binary(call, JavaOperator.DIV);
			case INT_MOD_INT:
				return binary(call, JavaOperator.MOD);
			case INT_AND_INT:
				return binary(call, JavaOperator.AND);
			case INT_OR_INT:
				return binary(call, JavaOperator.OR);
			case INT_XOR_INT:
				return binary(call, JavaOperator.XOR);
			case INT_SHL:
				return binary(call, JavaOperator.SHL);
			case INT_SHR:
				return binary(call, JavaOperator.SHR);
			case INT_USHR:
				return binary(call, JavaOperator.USHR);
			case INT_COUNT_LOW_ZEROES:
				return callAsStatic("Integer.numberOfTrailingZeros", call);
			case INT_COUNT_HIGH_ZEROES:
				return callAsStatic("Integer.numberOfLeadingZeros", call);
			case INT_COUNT_LOW_ONES:
				return new ExpressionString("Integer.numberOfTrailingZeros(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case INT_COUNT_HIGH_ONES:
				return new ExpressionString("Integer.numberOfLeadingZeros(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case UINT_NOT:
				return unaryPrefix(call, JavaOperator.INVERT);
			case UINT_ADD_UINT:
				return binary(call, JavaOperator.ADD);
			case UINT_SUB_UINT:
				return binary(call, JavaOperator.SUB);
			case UINT_MUL_UINT:
				return binary(call, JavaOperator.MUL);
			case UINT_DIV_UINT:
				return callAsStatic("Integer.divideUnsigned", call);
			case UINT_MOD_UINT:
				return callAsStatic("Integer.remainderUnsigned", call);
			case UINT_AND_UINT:
				return binary(call, JavaOperator.AND);
			case UINT_OR_UINT:
				return binary(call, JavaOperator.OR);
			case UINT_XOR_UINT:
				return binary(call, JavaOperator.XOR);
			case UINT_SHL:
				return binary(call, JavaOperator.SHL);
			case UINT_SHR:
				return binary(call, JavaOperator.USHR);
			case UINT_COUNT_LOW_ZEROES:
				return callAsStatic("Integer.numberOfTrailingZeros", call);
			case UINT_COUNT_HIGH_ZEROES:
				return callAsStatic("Integer.numberOfLeadingZeros", call);
			case UINT_COUNT_LOW_ONES:
				return new ExpressionString("Integer.numberOfTrailingZeros(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case UINT_COUNT_HIGH_ONES:
				return new ExpressionString("Integer.numberOfLeadingZeros(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case LONG_NOT:
				return unaryPrefix(call, JavaOperator.INVERT);
			case LONG_NEG:
				return unaryPrefix(call, JavaOperator.NEG);
			case LONG_ADD_LONG:
				return binary(call, JavaOperator.ADD);
			case LONG_SUB_LONG:
				return binary(call, JavaOperator.SUB);
			case LONG_MUL_LONG:
				return binary(call, JavaOperator.MUL);
			case LONG_DIV_LONG:
				return binary(call, JavaOperator.DIV);
			case LONG_MOD_LONG:
				return binary(call, JavaOperator.MOD);
			case LONG_AND_LONG:
				return binary(call, JavaOperator.AND);
			case LONG_OR_LONG:
				return binary(call, JavaOperator.OR);
			case LONG_XOR_LONG:
				return binary(call, JavaOperator.XOR);
			case LONG_SHL:
				return binary(call, JavaOperator.SHL);
			case LONG_SHR:
				return binary(call, JavaOperator.SHR);
			case LONG_USHR:
				return binary(call, JavaOperator.USHR);
			case LONG_COUNT_LOW_ZEROES:
				return callAsStatic("Long.numberOfTrailingZeros", call);
			case LONG_COUNT_HIGH_ZEROES:
				return callAsStatic("Long.numberOfLeadingZeros", call);
			case LONG_COUNT_LOW_ONES:
				return new ExpressionString("Long.numberOfTrailingZeros(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case LONG_COUNT_HIGH_ONES:
				return new ExpressionString("Long.numberOfLeadingZeros(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case ULONG_NOT:
				return unaryPrefix(call, JavaOperator.INVERT);
			case ULONG_ADD_ULONG:
				return binary(call, JavaOperator.ADD);
			case ULONG_SUB_ULONG:
				return binary(call, JavaOperator.SUB);
			case ULONG_MUL_ULONG:
				return binary(call, JavaOperator.MUL);
			case ULONG_DIV_ULONG:
				return callAsStatic("Long.divideUnsigned", call);
			case ULONG_MOD_ULONG:
				return callAsStatic("Long.remainderUnsigned", call);
			case ULONG_AND_ULONG:
				return binary(call, JavaOperator.AND);
			case ULONG_OR_ULONG:
				return binary(call, JavaOperator.OR);
			case ULONG_XOR_ULONG:
				return binary(call, JavaOperator.XOR);
			case ULONG_SHL:
				return binary(call, JavaOperator.SHL);
			case ULONG_SHR:
				return binary(call, JavaOperator.USHR);
			case ULONG_COUNT_LOW_ZEROES:
				return callAsStatic("Long.numberOfTrailingZeros", call);
			case ULONG_COUNT_HIGH_ZEROES:
				return callAsStatic("Long.numberOfLeadingZeros", call);
			case ULONG_COUNT_LOW_ONES:
				return new ExpressionString("Long.numberOfTrailingZeros(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case ULONG_COUNT_HIGH_ONES:
				return new ExpressionString("Long.numberOfLeadingZeros(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case USIZE_NOT:
				return unaryPrefix(call, JavaOperator.INVERT);
			case USIZE_ADD_USIZE:
				return binary(call, JavaOperator.ADD);
			case USIZE_SUB_USIZE:
				return binary(call, JavaOperator.SUB);
			case USIZE_MUL_USIZE:
				return binary(call, JavaOperator.MUL);
			case USIZE_DIV_USIZE:
				return binary(call, JavaOperator.DIV);
			case USIZE_MOD_USIZE:
				return binary(call, JavaOperator.MOD);
			case USIZE_AND_USIZE:
				return binary(call, JavaOperator.AND);
			case USIZE_OR_USIZE:
				return binary(call, JavaOperator.OR);
			case USIZE_XOR_USIZE:
				return binary(call, JavaOperator.XOR);
			case USIZE_SHL:
				return binary(call, JavaOperator.SHL);
			case USIZE_SHR:
				return binary(call, JavaOperator.USHR);
			case USIZE_COUNT_LOW_ZEROES:
				return callAsStatic("Integer.numberOfTrailingZeros", call);
			case USIZE_COUNT_HIGH_ZEROES:
				return callAsStatic("Integer.numberOfLeadingZeros", call);
			case USIZE_COUNT_LOW_ONES:
				return new ExpressionString("Integer.numberOfTrailingZeros(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case USIZE_COUNT_HIGH_ONES:
				return new ExpressionString("Integer.numberOfLeadingZeros(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case FLOAT_NEG:
				return unaryPrefix(call, JavaOperator.NEG);
			case FLOAT_ADD_FLOAT:
				return binary(call, JavaOperator.ADD);
			case FLOAT_SUB_FLOAT:
				return binary(call, JavaOperator.SUB);
			case FLOAT_MUL_FLOAT:
				return binary(call, JavaOperator.MUL);
			case FLOAT_DIV_FLOAT:
				return binary(call, JavaOperator.DIV);
			case FLOAT_MOD_FLOAT:
				return binary(call, JavaOperator.MOD);
			case DOUBLE_NEG:
				return unaryPrefix(call, JavaOperator.NEG);
			case DOUBLE_ADD_DOUBLE:
				return binary(call, JavaOperator.ADD);
			case DOUBLE_SUB_DOUBLE:
				return binary(call, JavaOperator.SUB);
			case DOUBLE_MUL_DOUBLE:
				return binary(call, JavaOperator.MUL);
			case DOUBLE_DIV_DOUBLE:
				return binary(call, JavaOperator.DIV);
			case DOUBLE_MOD_DOUBLE:
				return binary(call, JavaOperator.MOD);
			case CHAR_ADD_INT:
				return cast(binary(call, JavaOperator.ADD), "char");
			case CHAR_SUB_INT:
				return cast(binary(call, JavaOperator.SUB), "char");
			case CHAR_SUB_CHAR:
				return binary(call, JavaOperator.SUB);
			case CHAR_REMOVE_DIACRITICS:
				throw new UnsupportedOperationException("Not yet supported!");
			case CHAR_TO_LOWER_CASE:
				return callAsStatic("Character.toLowerCase", call);
			case CHAR_TO_UPPER_CASE:
				return callAsStatic("Character.toUpperCase", call);
			case STRING_ADD_STRING:
				return binary(call, JavaOperator.ADD);
			case STRING_INDEXGET:
				return call("charAt", call);
			case STRING_RANGEGET: {
				ExpressionString left = call.target.accept(this);
				Expression argument = call.arguments.arguments[0];
				if (argument instanceof RangeExpression) {
					RangeExpression rangeArgument = (RangeExpression) argument;
					ExpressionString from = rangeArgument.from.accept(this);
					if ((rangeArgument.to instanceof CallExpression) && ((CallExpression) rangeArgument.to).member.getBuiltin() == BuiltinID.STRING_LENGTH) {
						return new ExpressionString(left.value + ".substring(" + from.value + ")", JavaOperator.CALL);
					} else {
						ExpressionString to = rangeArgument.to.accept(this);
						return new ExpressionString(left.value + ".substring(" + from.value + ", " + to.value + ")", JavaOperator.CALL);
					}
				} else {
					Expression temp = duplicable(argument);
					ExpressionString from = new ExpressionString(temp.accept(this).value + ".from", JavaOperator.MEMBER);
					ExpressionString to = new ExpressionString(temp.accept(this).value + ".to", JavaOperator.MEMBER);
					return new ExpressionString(left.value + ".substring(" + from.value + ", " + to.value + ")", JavaOperator.CALL);
				}
			}
			case STRING_REMOVE_DIACRITICS:
				throw new UnsupportedOperationException("Not yet supported!");
			case STRING_TRIM:
				return call("trim", call);
			case STRING_TO_LOWER_CASE:
				return call("toLowerCase", call);
			case STRING_TO_UPPER_CASE:
				return call("toUpperCase", call);
			case ASSOC_INDEXGET:
				return call("get", call);
			case ASSOC_INDEXSET:
				return call("put", call);
			case ASSOC_CONTAINS:
				return call("containsKey", call);
			case ASSOC_GETORDEFAULT:
				return call("get", call);
			case ASSOC_EQUALS:
				throw new UnsupportedOperationException("Not yet supported!");
			case ASSOC_NOTEQUALS:
				throw new UnsupportedOperationException("Not yet supported!");
			case ASSOC_SAME:
				return binary(call, JavaOperator.EQUALS);
			case ASSOC_NOTSAME:
				return binary(call, JavaOperator.NOTEQUALS);
			case GENERICMAP_GETOPTIONAL:
				return call("get", call).unaryPrefix(JavaOperator.CAST, "(" + scope.type(call.type) + ")");
			case GENERICMAP_PUT:
				return call("put", call);
			case GENERICMAP_CONTAINS:
				return call("containsKey", call);
			case GENERICMAP_ADDALL:
				return call("putAll", call);
			case GENERICMAP_EQUALS:
				throw new UnsupportedOperationException("Not yet supported!");
			case GENERICMAP_NOTEQUALS:
				throw new UnsupportedOperationException("Not yet supported!");
			case GENERICMAP_SAME:
				return binary(call, JavaOperator.EQUALS);
			case GENERICMAP_NOTSAME:
				return binary(call, JavaOperator.NOTEQUALS);
			case ARRAY_INDEXGET:
				return new ExpressionString(call.target.accept(this) + "[" + call.arguments.arguments[0].accept(this) + "]", JavaOperator.INDEX);
			case ARRAY_INDEXSET: {
				ExpressionString value = call.arguments.arguments[1].accept(this);
				TypeID baseType = ((ArrayTypeID) (call.target.type)).elementType;
				String asType = "";
				if (baseType == BasicTypeID.BYTE) {
					asType = "(byte)";
				} else if (baseType == BasicTypeID.USHORT) {
					asType = "(short)";
				}

				if (!asType.isEmpty()) {
					if (value.priority == JavaOperator.CAST && value.value.startsWith("(int)"))
						value = new ExpressionString(asType + value.value.substring(5), value.priority);
					else
						value = value.unaryPrefix(JavaOperator.CAST, asType);
				}

				return new ExpressionString(
						call.target.accept(this)
								+ "[" + call.arguments.arguments[0].accept(this)
								+ "] = "
								+ value.value, JavaOperator.INDEX);
			}
			case ARRAY_INDEXGETRANGE: {
				ExpressionString left = call.target.accept(this);
				Expression argument = call.arguments.arguments[0];
				if (argument instanceof RangeExpression) {
					RangeExpression rangeArgument = (RangeExpression) argument;
					ExpressionString from = rangeArgument.from.accept(this);
					ExpressionString to = rangeArgument.to.accept(this);
					return new ExpressionString(scope.type(JavaClass.ARRAYS) + ".copyOfRange(" + left.value + ", " + from.value + ", " + to.value + ")", JavaOperator.CALL);
				} else {
					throw new UnsupportedOperationException("Not yet supported!");
				}
			}
			case ARRAY_CONTAINS: {
				JavaNativeMethod method = scope.fileScope.helperGenerator.createArrayContains((ArrayTypeID) call.target.type);
				return callAsStatic(scope.fileScope.importer.importType(method.cls) + '.' + method.name, call);
			}
			case ARRAY_EQUALS:
				return callAsStatic(scope.type(JavaClass.ARRAYS) + ".equals", call);
			case ARRAY_NOTEQUALS:
				return callAsStatic("!" + scope.type(JavaClass.ARRAYS) + ".equals", call);
			case ARRAY_SAME:
				return binary(call, JavaOperator.EQUALS);
			case ARRAY_NOTSAME:
				return binary(call, JavaOperator.NOTEQUALS);
			case FUNCTION_CALL: {
				StringBuilder output = new StringBuilder();
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
				return new ExpressionString(output.toString(), JavaOperator.CALL);
			}
			case FUNCTION_SAME:
				return binary(call, JavaOperator.EQUALS);
			case FUNCTION_NOTSAME:
				return binary(call, JavaOperator.NOTEQUALS);
			case OBJECT_SAME:
				return binary(call, JavaOperator.EQUALS);
			case OBJECT_NOTSAME:
				return binary(call, JavaOperator.NOTEQUALS);
			case OPTIONAL_IS_NULL:
				return call.target.type.withoutOptional() == BasicTypeID.USIZE
						? call.target.accept(this).unaryPostfix(JavaOperator.NOTEQUALS, " < 0")
						: call.target.accept(this).unaryPostfix(JavaOperator.EQUALS, " == null");
			case OPTIONAL_IS_NOT_NULL:
				return call.target.type.withoutOptional() == BasicTypeID.USIZE
						? call.target.accept(this).unaryPostfix(JavaOperator.NOTEQUALS, " >= 0")
						: call.target.accept(this).unaryPostfix(JavaOperator.NOTEQUALS, " != null");
		}

		throw new UnsupportedOperationException("Unknown builtin call: " + builtin);
	}

	private ExpressionString visitBuiltinCallStatic(CallStaticExpression call, BuiltinID builtin) {
		switch (builtin) {
			case BOOL_PARSE:
				return callStatic("Boolean.parseBoolean", call);
			case BYTE_PARSE:
				return callStatic("Integer.parseInt", call);
			case BYTE_PARSE_WITH_BASE:
				return callStatic("Integer.parseInt", call);
			case SBYTE_PARSE:
				return callStatic("Byte.parseByte", call);
			case SBYTE_PARSE_WITH_BASE:
				return callStatic("Byte.parseByte", call);
			case SHORT_PARSE:
				return callStatic("Short.parseShort", call);
			case SHORT_PARSE_WITH_BASE:
				return callStatic("Short.parseShort", call);
			case USHORT_PARSE:
				return callStatic("Integer.parseShort", call);
			case USHORT_PARSE_WITH_BASE:
				return callStatic("Integer.parseShort", call);
			case INT_PARSE:
			case USIZE_PARSE:
				return callStatic("Integer.parseInt", call);
			case INT_PARSE_WITH_BASE:
			case USIZE_PARSE_WITH_BASE:
				return callStatic("Integer.parseInt", call);
			case UINT_PARSE:
				return callStatic("Integer.parseUnsignedInt", call);
			case UINT_PARSE_WITH_BASE:
				return callStatic("Integer.parseUnsignedInt", call);
			case LONG_PARSE:
				return callStatic("Long.parseLong", call);
			case LONG_PARSE_WITH_BASE:
				return callStatic("Long.parseLong", call);
			case ULONG_PARSE:
				return callStatic("Long.parseUnsignedLong", call);
			case ULONG_PARSE_WITH_BASE:
				return callStatic("Long.parseUnsignedLong", call);
			case FLOAT_FROM_BITS:
				return callStatic("Float.intBitsToFloat", call);
			case FLOAT_PARSE:
				return callStatic("Float.parseFloat", call);
			case DOUBLE_FROM_BITS:
				return callStatic("Double.longBitsToDouble", call);
			case DOUBLE_PARSE:
				return callStatic("Double.parseDouble", call);
		}

		throw new UnsupportedOperationException("Unknown builtin static call: " + builtin);
	}

	private ExpressionString visitBuiltinCompare(CompareExpression call, BuiltinID builtin) {
		switch (builtin) {
			case BYTE_COMPARE:
				return compare(
						call.left.accept(this).unaryPostfix(JavaOperator.AND_FF),
						call.right.accept(this).unaryPostfix(JavaOperator.AND_FF),
						call.comparison);
			case SBYTE_COMPARE:
				return compare(call.left, call.right, call.comparison);
			case SHORT_COMPARE:
				return compare(call.left, call.right, call.comparison);
			case USHORT_COMPARE:
				return compare(
						call.left.accept(this).unaryPostfix(JavaOperator.AND_FFFF),
						call.right.accept(this).unaryPostfix(JavaOperator.AND_FFFF),
						call.comparison);
			case INT_COMPARE:
			case USIZE_COMPARE:
				return compare(call.left, call.right, call.comparison);
			case UINT_COMPARE:
			case USIZE_COMPARE_UINT:
				if (call.comparison == CompareType.EQ || call.comparison == CompareType.NE) {
					return compare(call.left, call.right, call.comparison);
				} else {
					return compare(callAsStatic("Integer.compareUnsigned", call.left, call.right), call.comparison);
				}
			case LONG_COMPARE:
			case LONG_COMPARE_INT:
				return compare(call.left, call.right, call.comparison);
			case ULONG_COMPARE:
			case ULONG_COMPARE_UINT:
				return compare(callAsStatic("Long.compareUnsigned", call.left, call.right), call.comparison);
			case FLOAT_COMPARE:
				return compare(call.left, call.right, call.comparison);
			case DOUBLE_COMPARE:
				return compare(call.left, call.right, call.comparison);
			case CHAR_COMPARE:
				return compare(call.left, call.right, call.comparison);
			case STRING_COMPARE:
			case ENUM_COMPARE:
				if (call.comparison == CompareType.EQ || call.comparison == CompareType.NE) {
					ExpressionString equals = call("equals", call.left, call.right);
					return call.comparison == CompareType.NE ? equals.unaryPrefix(JavaOperator.NOT) : equals;
				} else {
					return compare(call("compareTo", call.left, call.right), call.comparison);
				}
		}

		throw new UnsupportedOperationException("Unknown builtin comparator: " + builtin);
	}

	private ExpressionString visitBuiltinGetter(GetterExpression call, BuiltinID builtin) {
		switch (builtin) {
			case INT_HIGHEST_ONE_BIT:
			case UINT_HIGHEST_ONE_BIT:
			case USIZE_HIGHEST_ONE_BIT:
				return callAsStatic("Integer.highestOneBit", call);
			case INT_LOWEST_ONE_BIT:
			case UINT_LOWEST_ONE_BIT:
			case USIZE_LOWEST_ONE_BIT:
				return callAsStatic("Integer.lowestOneBit", call);
			case INT_HIGHEST_ZERO_BIT:
			case UINT_HIGHEST_ZERO_BIT:
			case USIZE_HIGHEST_ZERO_BIT:
				return new ExpressionString("Integer.highestOneBit(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case INT_LOWEST_ZERO_BIT:
			case UINT_LOWEST_ZERO_BIT:
			case USIZE_LOWEST_ZERO_BIT:
				return new ExpressionString("Integer.lowestOneBit(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case INT_BIT_COUNT:
			case UINT_BIT_COUNT:
			case USIZE_BIT_COUNT:
				return callAsStatic("Integer.bitCount", call);
			case LONG_HIGHEST_ONE_BIT:
			case ULONG_HIGHEST_ONE_BIT:
				return callAsStatic("Long.highestOneBit", call);
			case LONG_LOWEST_ONE_BIT:
			case ULONG_LOWEST_ONE_BIT:
				return callAsStatic("Long.lowestOneBit", call);
			case LONG_HIGHEST_ZERO_BIT:
			case ULONG_HIGHEST_ZERO_BIT:
				return new ExpressionString("Long.highestOneBit(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case LONG_LOWEST_ZERO_BIT:
			case ULONG_LOWEST_ZERO_BIT:
				return new ExpressionString("Long.lowestOneBit(~" + call.target.accept(this).value + ")", JavaOperator.CALL);
			case LONG_BIT_COUNT:
			case ULONG_BIT_COUNT:
				return callAsStatic("Long.bitCount", call);
			case FLOAT_BITS:
				return callAsStatic("Float.floatToRawIntBits", call);
			case DOUBLE_BITS:
				return callAsStatic("Double.doubleToRawLongBits", call);
			case STRING_LENGTH:
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".length()");
			case STRING_CHARACTERS:
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".toCharArray()");
			case STRING_ISEMPTY:
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".isEmpty()");
			case ASSOC_SIZE:
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".size()");
			case ASSOC_ISEMPTY:
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".isEmpty()");
			case ASSOC_KEYS: {
				AssocTypeID type = (AssocTypeID) call.target.type;
				ExpressionString keys = hoist(call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".keys()"), scope.type(type.keyType) + "[]");
				return keys.unaryPostfix(JavaOperator.CALL, ".toArray(new " + scope.type(type.keyType) + "[" + keys.value + ".length])");
			}
			case ASSOC_VALUES:
				AssocTypeID type = (AssocTypeID) call.target.type;
				ExpressionString values = hoist(call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".values()"), scope.type(type.valueType) + "[]");
				return values.unaryPostfix(JavaOperator.CALL, ".toArray(new " + scope.type(type.valueType) + "[" + values.value + ".length])");
			case ASSOC_HASHCODE:
				// TODO: we need a content-based hashcode
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".hashCode()");
			case GENERICMAP_SIZE:
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".size()");
			case GENERICMAP_ISEMPTY:
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".isEmpty()");
			case GENERICMAP_HASHCODE:
				// TODO: we need a content-based hashcode
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".hashCode()");
			case ARRAY_LENGTH:
				return call.target.accept(this).unaryPostfix(JavaOperator.MEMBER, ".length");
			case ARRAY_HASHCODE:
				return callAsStatic(scope.type(JavaClass.ARRAYS) + ".deepHashCode", call);
			case ARRAY_ISEMPTY:
				return call.target.accept(this).unaryPostfix(JavaOperator.EQUALS, ".length == 0");
			case ENUM_NAME:
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".name()");
			case ENUM_ORDINAL:
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".ordinal()");
			case OBJECT_HASHCODE:
				return call.target.accept(this).unaryPostfix(JavaOperator.CALL, ".hashCode()");
			case RANGE_FROM:
				return call.target.accept(this).unaryPostfix(JavaOperator.MEMBER, ".from");
			case RANGE_TO:
				return call.target.accept(this).unaryPostfix(JavaOperator.MEMBER, ".to");
		}

		throw new UnsupportedOperationException("Unknown builtin getter: " + builtin);
	}

	private ExpressionString visitBuiltinConstant(ConstExpression call, BuiltinID builtin) {
		switch (builtin) {
			case BYTE_GET_MIN_VALUE:
				return new ExpressionString("0", JavaOperator.PRIMARY);
			case BYTE_GET_MAX_VALUE:
				return new ExpressionString("255", JavaOperator.PRIMARY);
			case SBYTE_GET_MIN_VALUE:
				return new ExpressionString("Byte.MIN_VALUE", JavaOperator.MEMBER);
			case SBYTE_GET_MAX_VALUE:
				return new ExpressionString("Byte.MAX_VALUE", JavaOperator.MEMBER);
			case SHORT_GET_MIN_VALUE:
				return new ExpressionString("Short.MIN_VALUE", JavaOperator.MEMBER);
			case SHORT_GET_MAX_VALUE:
				return new ExpressionString("Short.MAX_VALUE", JavaOperator.MEMBER);
			case USHORT_GET_MIN_VALUE:
				return new ExpressionString("0", JavaOperator.PRIMARY);
			case USHORT_GET_MAX_VALUE:
				return new ExpressionString("65535", JavaOperator.PRIMARY);
			case INT_GET_MIN_VALUE:
				return new ExpressionString("Integer.MIN_VALUE", JavaOperator.MEMBER);
			case INT_GET_MAX_VALUE:
				return new ExpressionString("Integer.MAX_VALUE", JavaOperator.MEMBER);
			case UINT_GET_MIN_VALUE:
				return new ExpressionString("0", JavaOperator.PRIMARY);
			case UINT_GET_MAX_VALUE:
				return new ExpressionString("-1", JavaOperator.PRIMARY);
			case LONG_GET_MIN_VALUE:
				return new ExpressionString("Long.MIN_VALUE", JavaOperator.MEMBER);
			case LONG_GET_MAX_VALUE:
				return new ExpressionString("Long.MAX_VALUE", JavaOperator.MEMBER);
			case ULONG_GET_MIN_VALUE:
				return new ExpressionString("0", JavaOperator.PRIMARY);
			case ULONG_GET_MAX_VALUE:
				return new ExpressionString("-1", JavaOperator.PRIMARY);
			case USIZE_GET_MIN_VALUE:
				return new ExpressionString("0", JavaOperator.PRIMARY);
			case USIZE_GET_MAX_VALUE:
				return new ExpressionString("Integer.MAX_VALUE", JavaOperator.MEMBER);
			case USIZE_BITS:
				return new ExpressionString("32", JavaOperator.PRIMARY);
			case FLOAT_GET_MIN_VALUE:
				return new ExpressionString("Float.MIN_VALUE", JavaOperator.MEMBER);
			case FLOAT_GET_MAX_VALUE:
				return new ExpressionString("Float.MAX_VALUE", JavaOperator.MEMBER);
			case DOUBLE_GET_MIN_VALUE:
				return new ExpressionString("Double.MIN_VALUE", JavaOperator.MEMBER);
			case DOUBLE_GET_MAX_VALUE:
				return new ExpressionString("Double.MAX_VALUE", JavaOperator.MEMBER);
			case CHAR_GET_MIN_VALUE:
				return new ExpressionString("Character.MIN_VALUE", JavaOperator.MEMBER);
			case CHAR_GET_MAX_VALUE:
				return new ExpressionString("Character.MAX_VALUE", JavaOperator.MEMBER);
			case ENUM_VALUES:
				return new ExpressionString(scope.fileScope.importer.importType(call.constant.member.definition) + ".values()", JavaOperator.CALL);
		}

		throw new UnsupportedOperationException("Unknown builtin static getter: " + builtin);
	}

	private ExpressionString visitBuiltinCast(CallExpression cast) throws CompileException {
		switch (cast.member.member.builtin) {
			case BOOL_TO_STRING:
				return callStatic("Boolean.toString", cast.target);
			case BYTE_TO_SBYTE:
				return cast(cast, "byte");
			case BYTE_TO_SHORT:
				return cast(castPostfix(cast, JavaOperator.AND_FF), "short");
			case BYTE_TO_USHORT:
				return castPostfix(cast, JavaOperator.AND_FF);
			case BYTE_TO_INT:
				return castPostfix(cast, JavaOperator.AND_FF);
			case BYTE_TO_UINT:
				return castPostfix(cast, JavaOperator.AND_FF);
			case BYTE_TO_USIZE:
				return castPostfix(cast, JavaOperator.AND_FF);
			case BYTE_TO_LONG:
				return castPostfix(cast, JavaOperator.AND_FFL);
			case BYTE_TO_ULONG:
				return castPostfix(cast, JavaOperator.AND_FFL);
			case BYTE_TO_FLOAT:
				cast(castPostfix(cast, JavaOperator.AND_FF), "float");
			case BYTE_TO_DOUBLE:
				cast(castPostfix(cast, JavaOperator.AND_FF), "double");
			case BYTE_TO_CHAR:
				return castImplicit(cast, "char");
			case BYTE_TO_STRING:
				return callStatic("Integer.toString", castPostfix(cast, JavaOperator.AND_FF));
			case SBYTE_TO_BYTE:
				return castImplicit(cast, "int");
			case SBYTE_TO_SHORT:
				return castImplicit(cast, "short");
			case SBYTE_TO_USHORT:
				return castImplicit(cast, "int");
			case SBYTE_TO_INT:
				return castImplicit(cast, "int");
			case SBYTE_TO_UINT:
				return castImplicit(cast, "int");
			case SBYTE_TO_USIZE:
				return castImplicit(cast, "int");
			case SBYTE_TO_LONG:
				return castImplicit(cast, "long");
			case SBYTE_TO_ULONG:
				return castImplicit(cast, "long");
			case SBYTE_TO_FLOAT:
				return castImplicit(cast, "float");
			case SBYTE_TO_DOUBLE:
				return castImplicit(cast, "double");
			case SBYTE_TO_CHAR:
				return castImplicit(cast, "char");
			case SBYTE_TO_STRING:
				return callStatic("Byte.toString", cast.target);
			case SHORT_TO_BYTE:
				return castImplicit(cast, "int");
			case SHORT_TO_SBYTE:
				return castImplicit(cast, "byte");
			case SHORT_TO_USHORT:
				return castImplicit(cast, "int");
			case SHORT_TO_INT:
				return castImplicit(cast, "int");
			case SHORT_TO_UINT:
				return castImplicit(cast, "int");
			case SHORT_TO_USIZE:
				return castImplicit(cast, "int");
			case SHORT_TO_LONG:
				return castImplicit(cast, "long");
			case SHORT_TO_ULONG:
				return castImplicit(cast, "long");
			case SHORT_TO_FLOAT:
				return castImplicit(cast, "float");
			case SHORT_TO_DOUBLE:
				return castImplicit(cast, "double");
			case SHORT_TO_CHAR:
				return castImplicit(cast, "char");
			case SHORT_TO_STRING:
				return callStatic("Short.toString", cast.target);
			case USHORT_TO_BYTE:
				return castImplicit(cast, "int");
			case USHORT_TO_SBYTE:
				return cast(cast, "byte");
			case USHORT_TO_SHORT:
				return cast(cast, "short");
			case USHORT_TO_INT:
				return castPostfix(cast, JavaOperator.AND_FFFF);
			case USHORT_TO_UINT:
				return castPostfix(cast, JavaOperator.AND_FFFF);
			case USHORT_TO_LONG:
				return castPostfix(cast, JavaOperator.AND_FFFFL);
			case USHORT_TO_ULONG:
				return castPostfix(cast, JavaOperator.AND_FFFFL);
			case USHORT_TO_USIZE:
				return castPostfix(cast, JavaOperator.AND_FFFF);
			case USHORT_TO_FLOAT:
				return cast.isImplicit ? cast.target.accept(this) : cast(castPostfix(cast, JavaOperator.AND_FFFF), "float");
			case USHORT_TO_DOUBLE:
				return cast.isImplicit ? cast.target.accept(this) : cast(castPostfix(cast, JavaOperator.AND_FFFF), "double");
			case USHORT_TO_CHAR:
				return castImplicit(cast, "char");
			case USHORT_TO_STRING:
				return callStatic("Integer.toString", castPostfix(cast, JavaOperator.AND_FFFF));
			case INT_TO_BYTE:
				return cast.target.accept(this);
			case INT_TO_SBYTE:
				return cast(cast, "byte");
			case INT_TO_SHORT:
				return cast(cast, "short");
			case INT_TO_USHORT:
				return cast.target.accept(this);
			case INT_TO_UINT:
				return cast.target.accept(this);
			case INT_TO_LONG:
				return castImplicit(cast, "long");
			case INT_TO_ULONG:
				return castImplicit(cast, "long");
			case INT_TO_FLOAT:
				return castImplicit(cast, "float");
			case INT_TO_DOUBLE:
				return castImplicit(cast, "double");
			case INT_TO_CHAR:
				return cast(cast, "char");
			case INT_TO_STRING:
				return callStatic("Integer.toString", cast.target);
			case INT_TO_USIZE:
				return cast.target.accept(this);
			case UINT_TO_BYTE:
				return cast.target.accept(this);
			case UINT_TO_SBYTE:
				return cast(cast, "byte");
			case UINT_TO_SHORT:
				return cast(cast, "short");
			case UINT_TO_USHORT:
				return cast.target.accept(this);
			case UINT_TO_INT:
				return cast.target.accept(this);
			case UINT_TO_LONG:
				return castImplicit(cast, "long");
			case UINT_TO_ULONG:
				return castPostfix(cast, JavaOperator.AND_8FL);
			case UINT_TO_USIZE:
				return cast.target.accept(this);
			case UINT_TO_FLOAT:
				return cast(castPostfix(cast, JavaOperator.AND_8FL), "float");
			case UINT_TO_DOUBLE:
				return cast(castPostfix(cast, JavaOperator.AND_8FL), "double");
			case UINT_TO_CHAR:
				return cast(cast, "char");
			case UINT_TO_STRING:
				return callStatic("Integer.toUnsignedString", cast.target);
			case LONG_TO_BYTE:
				return cast(cast, "int");
			case LONG_TO_SBYTE:
				return cast(cast, "byte");
			case LONG_TO_SHORT:
				return cast(cast, "short");
			case LONG_TO_USHORT:
				return cast(cast, "int");
			case LONG_TO_INT:
				return cast(cast, "int");
			case LONG_TO_UINT:
				return cast(cast, "int");
			case LONG_TO_ULONG:
				return cast.target.accept(this);
			case LONG_TO_USIZE:
				return cast(cast, "int");
			case LONG_TO_FLOAT:
				return castImplicit(cast, "float");
			case LONG_TO_DOUBLE:
				return castImplicit(cast, "double");
			case LONG_TO_CHAR:
				return cast(cast, "char");
			case LONG_TO_STRING:
				return callStatic("Long.toString", cast.target);
			case ULONG_TO_BYTE:
				return cast(cast, "int");
			case ULONG_TO_SBYTE:
				return cast(cast, "byte");
			case ULONG_TO_SHORT:
				return cast(cast, "short");
			case ULONG_TO_USHORT:
				return cast(cast, "int");
			case ULONG_TO_INT:
				return cast(cast, "int");
			case ULONG_TO_UINT:
				return cast(cast, "int");
			case ULONG_TO_LONG:
				return cast.target.accept(this);
			case ULONG_TO_USIZE:
				return cast(cast, "int");
			case ULONG_TO_FLOAT:
				return castImplicit(cast, "float"); // TODO: this is incorrect!
			case ULONG_TO_DOUBLE:
				return castImplicit(cast, "double"); // TODO: this is incorrect!
			case ULONG_TO_CHAR:
				return cast(cast, "char");
			case ULONG_TO_STRING:
				return callStatic("Long.toUnsignedString", cast.target);
			case USIZE_TO_BYTE:
				return cast.target.accept(this);
			case USIZE_TO_SBYTE:
				return cast(cast, "byte");
			case USIZE_TO_SHORT:
				return cast(cast, "short");
			case USIZE_TO_USHORT:
				return cast.target.accept(this);
			case USIZE_TO_INT:
				return cast.target.accept(this);
			case USIZE_TO_UINT:
				return cast.target.accept(this);
			case USIZE_TO_LONG:
				return castImplicit(cast, "long");
			case USIZE_TO_ULONG:
				return castImplicit(cast, "long");
			case USIZE_TO_FLOAT:
				return castImplicit(cast, "float");
			case USIZE_TO_DOUBLE:
				return castImplicit(cast, "double");
			case FLOAT_TO_BYTE:
				return cast(cast, "int");
			case FLOAT_TO_SBYTE:
				return cast(cast, "byte");
			case FLOAT_TO_SHORT:
				return cast(cast, "short");
			case FLOAT_TO_USHORT:
				return cast(cast, "int");
			case FLOAT_TO_INT:
				return cast(cast, "int");
			case FLOAT_TO_UINT:
				return cast(cast, "int");
			case FLOAT_TO_LONG:
				return cast(cast, "long");
			case FLOAT_TO_ULONG:
				return cast(cast, "long");
			case FLOAT_TO_USIZE:
				return cast(cast, "int");
			case FLOAT_TO_DOUBLE:
				return castImplicit(cast, "double");
			case FLOAT_TO_STRING:
				return callStatic("Float.toString", cast.target);
			case DOUBLE_TO_BYTE:
				return cast(cast, "int");
			case DOUBLE_TO_SBYTE:
				return cast(cast, "byte");
			case DOUBLE_TO_SHORT:
				return cast(cast, "short");
			case DOUBLE_TO_USHORT:
				return cast(cast, "int");
			case DOUBLE_TO_INT:
				return cast(cast, "int");
			case DOUBLE_TO_UINT:
				return cast(cast, "int");
			case DOUBLE_TO_LONG:
				return cast(cast, "long");
			case DOUBLE_TO_ULONG:
				return cast(cast, "long");
			case DOUBLE_TO_USIZE:
				return cast(cast, "int");
			case DOUBLE_TO_FLOAT:
				return cast(cast, "float");
			case DOUBLE_TO_STRING:
				return callStatic("Double.toString", cast.target);
			case CHAR_TO_BYTE:
				return cast.target.accept(this);
			case CHAR_TO_SBYTE:
				return cast(cast, "byte");
			case CHAR_TO_SHORT:
				return cast(cast, "short");
			case CHAR_TO_USHORT:
				return castImplicit(cast, "int");
			case CHAR_TO_INT:
				return castImplicit(cast, "int");
			case CHAR_TO_UINT:
				return castImplicit(cast, "int");
			case CHAR_TO_LONG:
				return castImplicit(cast, "long");
			case CHAR_TO_ULONG:
				return castImplicit(cast, "long");
			case CHAR_TO_USIZE:
				return castImplicit(cast, "int");
			case CHAR_TO_STRING:
				return callStatic("Character.toString", cast.target);
			case ENUM_TO_STRING:
				return cast.target.accept(this).unaryPostfix(JavaOperator.TOSTRING);
			case BYTE_ARRAY_AS_SBYTE_ARRAY:
			case SBYTE_ARRAY_AS_BYTE_ARRAY:
			case SHORT_ARRAY_AS_USHORT_ARRAY:
			case USHORT_ARRAY_AS_SHORT_ARRAY:
			case INT_ARRAY_AS_UINT_ARRAY:
			case UINT_ARRAY_AS_INT_ARRAY:
			case LONG_ARRAY_AS_ULONG_ARRAY:
			case ULONG_ARRAY_AS_LONG_ARRAY:
				return cast.target.accept(this);
		}

		throw new UnsupportedOperationException("Unknown builtin cast: " + cast.member.member.builtin);
	}

	private ExpressionString visitBuiltinConstructor(NewExpression expression) throws CompileException {
		switch (expression.constructor.getBuiltin()) {
			case STRING_CONSTRUCTOR_CHARACTERS:
				return callStatic("new String", expression.arguments.arguments[0]);
			case ASSOC_CONSTRUCTOR: {
				String typeName = scope.type(new JavaClass("java.util", "HashMap", JavaClass.Kind.CLASS));
				AssocTypeID type = (AssocTypeID) expression.type;

				StringBuilder result = new StringBuilder();
				result.append("new ").append(typeName).append("<");
				//result.append(scope.type(type.keyType));
				//result.append(", ");
				//result.append(scope.type(type.valueType));
				result.append(">()");
				return new ExpressionString(result.toString(), JavaOperator.NEW);
			}
			case GENERICMAP_CONSTRUCTOR: {
				String typeName = scope.type(new JavaClass("java.util", "HashMap", JavaClass.Kind.CLASS));
				StringBuilder result = new StringBuilder();
				result.append("new ").append(typeName).append("<Class<?>, Object>()");
				return new ExpressionString(result.toString(), JavaOperator.NEW);
			}
			case ARRAY_CONSTRUCTOR_SIZED: {
				ArrayTypeID type = (ArrayTypeID) expression.type;
				if (type.dimension == 1) {
					ExpressionString size = expression.arguments.arguments[0].accept(this);
					return newArray(type.elementType, size);
				} else {
					// TODO: implement
					throw new UnsupportedOperationException("Not yet supported!");
				}
			}
			case ARRAY_CONSTRUCTOR_INITIAL_VALUE: {
				ArrayTypeID type = (ArrayTypeID) expression.type;

				if (type.dimension == 1) {
					ExpressionString size = duplicable(expression.arguments.arguments[0]).accept(this);
					ExpressionString value = duplicable(expression.arguments.arguments[1]).accept(this);
					String temp = scope.createTempVariable();
					String tempI = scope.createTempVariable();
					target.writeLine(new StringBuilder()
							.append(scope.type(type))
							.append(' ')
							.append(temp)
							.append(" = ")
							.append(newArray(type.elementType, size).value)
							.append(";")
							.toString());
					target.writeLine(new StringBuilder()
							.append("for (int ")
							.append(tempI)
							.append(" = 0; ")
							.append(tempI)
							.append(" < ")
							.append(temp)
							.append(".length; ")
							.append(tempI)
							.append("++)")
							.toString());
					target.writeLine(new StringBuilder()
							.append(scope.settings.indent)
							.append(temp)
							.append("[")
							.append(tempI)
							.append("] = ")
							.append(value.value)
							.append(";")
							.toString());
					return new ExpressionString(temp, JavaOperator.PRIMARY);
				} else {
					// TODO: implement
					throw new UnsupportedOperationException("Not yet supported!");
				}
			}
			case ARRAY_CONSTRUCTOR_LAMBDA: {
				ArrayTypeID type = (ArrayTypeID) expression.type;

				if (type.dimension == 1) {
					Expression lambda = expression.arguments.arguments[1];
					ExpressionString lambdaString = null;
					if (!(lambda instanceof FunctionExpression))
						lambdaString = duplicable(lambda).accept(this);

					ExpressionString size = duplicable(expression.arguments.arguments[0]).accept(this);
					String temp = scope.createTempVariable();
					target.writeLine(new StringBuilder()
							.append(scope.type(type))
							.append(' ')
							.append(temp)
							.append(" = ")
							.append(newArray(type.elementType, size))
							.append(";")
							.toString());
					VarStatement tempI = new VarStatement(expression.position, new VariableID(), scope.createTempVariable(), BasicTypeID.INT, null, true);
					target.writeLine(new StringBuilder()
							.append("for (int ")
							.append(tempI.name)
							.append(" = 0; ")
							.append(tempI.name)
							.append(" < ")
							.append(temp)
							.append(".length; ")
							.append(tempI.name)
							.append("++)")
							.toString());

					if (lambdaString == null) {
						FunctionExpression lambdaFunction = (FunctionExpression) lambda;
						Expression lambdaExpression = lambdaFunction.asReturnExpression(new GetLocalVariableExpression(expression.position, tempI));
						if (lambdaExpression != null) {
							// use expression directly
							target.writeLine(new StringBuilder()
									.append(temp)
									.append("[")
									.append(tempI.name)
									.append("] = ")
									.append(lambdaExpression.accept(this).value)
									.append(";")
									.toString());
						} else {
							// turn it into a function
							throw new UnsupportedOperationException("Not yet supported!");
						}
					} else {
						JavaSynthesizedFunctionInstance function = scope.context.getFunction((FunctionTypeID) lambda.type);
						target.writeLine(new StringBuilder()
								.append(scope.settings.indent)
								.append(temp)
								.append("[")
								.append(tempI.name)
								.append("] = ")
								.append(lambdaString.value)
								.append(".").append(function.getMethod()).append("(")
								.append(tempI.name)
								.append(");")
								.toString());
					}

					return new ExpressionString(temp, JavaOperator.PRIMARY);
				} else {
					// TODO: implement
					throw new UnsupportedOperationException("Not yet supported!");
				}
			}
			case ARRAY_CONSTRUCTOR_PROJECTED: {
				ArrayTypeID type = (ArrayTypeID) expression.type;

				if (type.dimension == 1) {
					Expression original = duplicable(expression.arguments.arguments[0]);
					ExpressionString originalString = original.accept(this);
					Expression lambda = expression.arguments.arguments[1];
					ExpressionString lambdaString = null;
					if (!(lambda instanceof FunctionExpression))
						lambdaString = duplicable(lambda).accept(this);

					String temp = scope.createTempVariable();
					target.writeLine(new StringBuilder()
							.append(scope.type(type))
							.append(' ')
							.append(temp)
							.append(" = ")
							.append(newArray(type.elementType, originalString.unaryPostfix(JavaOperator.MEMBER, ".length")))
							.append(";")
							.toString());
					VarStatement tempI = new VarStatement(expression.position, new VariableID(), scope.createTempVariable(), BasicTypeID.INT, null, true);
					target.writeLine(new StringBuilder()
							.append("for (int ")
							.append(tempI.name)
							.append(" = 0; ")
							.append(tempI.name)
							.append(" < ")
							.append(temp)
							.append(".length; ")
							.append(tempI.name)
							.append("++)")
							.toString());

					if (lambdaString == null) {
						FunctionExpression lambdaFunction = (FunctionExpression) lambda;
						TypeMembers originalArrayTypeMembers = scope.fileScope.semanticScope.getTypeMembers(expression.arguments.arguments[0].type);
						Expression getOriginalValue = originalArrayTypeMembers
								.getOrCreateGroup(OperatorType.INDEXGET)
								.call(expression.position, scope.fileScope.semanticScope, original, new CallArguments(new GetLocalVariableExpression(expression.position, tempI)), true);
						Expression lambdaExpression = lambdaFunction.asReturnExpression(getOriginalValue);
						if (lambdaExpression != null) {
							// use expression directly
							target.writeLine(new StringBuilder()
									.append(temp)
									.append("[")
									.append(tempI.name)
									.append("] = ")
									.append(lambdaExpression.accept(this).value)
									.append(";")
									.toString());
						} else {
							// turn it into a function
							throw new UnsupportedOperationException("Not yet supported!");
						}
					} else {
						JavaSynthesizedFunctionInstance function = scope.context.getFunction((FunctionTypeID) lambda.type);
						target.writeLine(new StringBuilder()
								.append(scope.settings.indent)
								.append(temp)
								.append("[")
								.append(tempI.name)
								.append("] = ")
								.append(lambdaString.value)
								.append(".").append(function.getMethod()).append("(")
								.append(originalString.value)
								.append("[")
								.append(tempI.name)
								.append("]);")
								.toString());
					}

					return new ExpressionString(temp, JavaOperator.PRIMARY);
				} else {
					// TODO: implement
					throw new UnsupportedOperationException("Not yet supported!");
				}
			}
			case ARRAY_CONSTRUCTOR_PROJECTED_INDEXED: {
				ArrayTypeID type = (ArrayTypeID) expression.type;

				if (type.dimension == 1) {
					Expression original = duplicable(expression.arguments.arguments[0]);
					ExpressionString originalString = original.accept(this);
					Expression lambda = expression.arguments.arguments[1];
					ExpressionString lambdaString = null;
					if (!(lambda instanceof FunctionExpression))
						lambdaString = duplicable(lambda).accept(this);

					String temp = scope.createTempVariable();
					target.writeLine(new StringBuilder()
							.append(scope.type(type))
							.append(' ')
							.append(temp)
							.append(" = ")
							.append(newArray(type.elementType, originalString.unaryPostfix(JavaOperator.MEMBER, ".length")))
							.append(";")
							.toString());
					VarStatement tempI = new VarStatement(expression.position, new VariableID(), scope.createTempVariable(), BasicTypeID.INT, null, true);
					target.writeLine(new StringBuilder()
							.append("for (int ")
							.append(tempI.name)
							.append(" = 0; ")
							.append(tempI.name)
							.append(" < ")
							.append(temp)
							.append(".length; ")
							.append(tempI.name)
							.append("++)")
							.toString());

					if (lambdaString == null) {
						FunctionExpression lambdaFunction = (FunctionExpression) lambda;
						TypeMembers originalArrayTypeMembers = scope.fileScope.semanticScope.getTypeMembers(expression.arguments.arguments[0].type);
						Expression getOriginalValue = originalArrayTypeMembers
								.getOrCreateGroup(OperatorType.INDEXGET)
								.call(expression.position, scope.fileScope.semanticScope, original, new CallArguments(new GetLocalVariableExpression(expression.position, tempI)), true);
						Expression lambdaExpression = lambdaFunction.asReturnExpression(
								getOriginalValue,
								new GetLocalVariableExpression(expression.position, tempI));
						if (lambdaExpression != null) {
							// use expression directly
							target.writeLine(new StringBuilder()
									.append(temp)
									.append("[")
									.append(tempI.name)
									.append("] = ")
									.append(lambdaExpression.accept(this).value)
									.append(";")
									.toString());
						} else {
							// turn it into a function
							throw new UnsupportedOperationException("Not yet supported!");
						}
					} else {
						JavaSynthesizedFunctionInstance function = scope.context.getFunction((FunctionTypeID) lambda.type);
						target.writeLine(new StringBuilder()
								.append(scope.settings.indent)
								.append(temp)
								.append("[")
								.append(tempI.name)
								.append("] = ")
								.append(lambdaString.value)
								.append(".").append(function.getMethod()).append("(")
								.append(tempI.name)
								.append(", ")
								.append(originalString.value)
								.append("[")
								.append(tempI.name)
								.append("]);")
								.toString());
					}

					return new ExpressionString(temp, JavaOperator.PRIMARY);
				} else {
					// TODO: implement
					throw new UnsupportedOperationException("Not yet supported!");
				}
			}
			case CLASS_DEFAULT_CONSTRUCTOR:
				return new ExpressionString("new " + scope.type(expression.type) + "()", JavaOperator.NEW);
		}

		throw new UnsupportedOperationException("Unknown builtin constructor: " + expression.constructor.getBuiltin());
	}

	@Override
	public ExpressionString isEmptyAsLengthZero(Expression value) {
		return value.accept(this).unaryPostfix(JavaOperator.EQUALS, ".length() == 0");
	}

	@Override
	public ExpressionString listToArray(CallExpression expression) {
		Expression target = duplicable(expression.target);
		ExpressionString targetString = target.accept(this);
		ArrayTypeID resultType = (ArrayTypeID) expression.type;
		return new ExpressionString(
				targetString.value + ".toArray(" + newArray(resultType.elementType, targetString.unaryPostfix(JavaOperator.CALL, ".size()")).value + ")",
				JavaOperator.CALL);
	}

	@Override
	public ExpressionString containsAsIndexOf(Expression target, Expression value) {
		return target.accept(this)
				.unaryPostfix(JavaOperator.GREATER_EQUALS, ".indexOf(" + value.accept(this).value + ") >= 0");
	}

	@Override
	public ExpressionString sorted(Expression value) {
		Expression target = duplicable(value);
		ExpressionString targetString = target.accept(this);
		ExpressionString copy = new ExpressionString(scope.type(JavaClass.ARRAYS) + ".copyOf(" + targetString.value + ", " + targetString.value + ".length).sort()", JavaOperator.CALL);
		ExpressionString source = hoist(copy, scope.type(target.type));
		this.target.writeLine(scope.type(JavaClass.ARRAYS) + ".sort(" + source.value + ");");
		return source;
	}

	@Override
	public ExpressionString sortedWithComparator(Expression value, Expression comparator) {
		Expression target = duplicable(value);
		ExpressionString comparatorString = comparator.accept(this);
		ExpressionString targetString = target.accept(this);
		ExpressionString copy = new ExpressionString(scope.type(JavaClass.ARRAYS) + ".copyOf(" + targetString.value + ", " + targetString.value + ".length).sort()", JavaOperator.CALL);
		ExpressionString source = hoist(copy, scope.type(target.type));
		this.target.writeLine(scope.type(JavaClass.ARRAYS) + ".sort(" + source.value + ", " + comparatorString.value + ");");
		return source;
	}

	@Override
	public ExpressionString arrayCopy(Expression value) {
		Expression target = duplicable(value);
		ExpressionString source = target.accept(this);
		return new ExpressionString(scope.type(JavaClass.ARRAYS) + ".copyOf(" + source.value + ", " + source.value + ".length)", JavaOperator.CALL);
	}

	@Override
	public ExpressionString arrayCopyResize(CallExpression call) {
		Expression source = call.target;
		Expression size = call.getFirstArgument();
		return new ExpressionString(scope.type(JavaClass.ARRAYS) + ".copyOf(" + source.accept(this) + ", " + size.accept(this) +")", JavaOperator.CALL);
	}

	@Override
	public ExpressionString arrayCopyTo(CallExpression call) {
		Expression source = call.target;
		Expression target = call.arguments.arguments[0];
		Expression sourceOffset = call.arguments.arguments[1];
		Expression targetOffset = call.arguments.arguments[2];
		Expression length = call.arguments.arguments[3];
		return new ExpressionString("System.arraycopy("
				+ source.accept(this) + ", "
				+ sourceOffset.accept(this) + ", "
				+ target.accept(this) + ", "
				+ targetOffset.accept(this) + ", "
				+ length.accept(this) + ")", JavaOperator.CALL);
	}

	@Override
	public ExpressionString stringToAscii(Expression value) {
		String standardCharsets = scope.type(STANDARD_CHARSETS);
		return value.accept(this).unaryPostfix(JavaOperator.CALL, ".getBytes(" + standardCharsets + ".US_ASCII)");
	}

	@Override
	public ExpressionString stringToUTF8(Expression value) {
		String standardCharsets = scope.type(STANDARD_CHARSETS);
		return value.accept(this).unaryPostfix(JavaOperator.CALL, ".getBytes(" + standardCharsets + ".UTF_8)");
	}

	@Override
	public ExpressionString bytesAsciiToString(Expression value) {
		String standardCharsets = scope.type(STANDARD_CHARSETS);
		return new ExpressionString(
				"new String(" + value.accept(this).value + ", " + standardCharsets + ".US_ASCII)",
				JavaOperator.NEW);
	}

	@Override
	public ExpressionString bytesUTF8ToString(Expression value) {
		String standardCharsets = scope.type(STANDARD_CHARSETS);
		return new ExpressionString(
				"new String(" + value.accept(this).value + ", " + standardCharsets + ".UTF_8)",
				JavaOperator.NEW);
	}
}
