package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.expression.captured.CapturedExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.modifiable.ModifiableExpression;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javabytecode.JavaMangler;
import org.openzen.zenscript.javabytecode.compiler.JavaModificationExpressionVisitor.PushOption;
import org.openzen.zenscript.javabytecode.compiler.lambda.LambdaIndyCompiler;
import org.openzen.zenscript.javabytecode.compiler.lambda.capturing.JavaInvalidCapturedExpressionVisitor;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.expressions.JavaFunctionInterfaceCastExpression;
import org.openzen.zenscript.javashared.types.JavaFunctionalInterfaceTypeID;

import java.util.*;

import static org.openzen.zenscript.javabytecode.compiler.JavaMethodBytecodeCompiler.OBJECT_HASHCODE;

public class JavaExpressionVisitor implements ExpressionVisitor<Void> {
	private static final JavaNativeMethod MAP_PUT = JavaNativeMethod.getInterface(JavaClass.MAP, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
	private static final JavaNativeMethod ARRAY_NEWINSTANCE = JavaNativeMethod.getNativeStatic(JavaClass.ARRAY, "newInstance", "(Ljava/lang/Class;I)Ljava/lang/Object;");
	private static final MethodID CONSTRUCTOR = MethodID.staticOperator(OperatorType.CONSTRUCTOR);

	final JavaWriter javaWriter;
	final JavaBytecodeContext context;
	final JavaCompiledModule module;
	private final JavaMangler javaMangler;
	private final JavaBoxingTypeVisitor boxingTypeVisitor;
	private final JavaBoxingTypeVisitor optionalWrappingTypeVisitor;
	private final JavaUnboxingTypeVisitor unboxingTypeVisitor;
	private final JavaUnboxingTypeVisitor optionalUnwrappingTypeVisitor;
	private final JavaFieldBytecodeCompiler fieldCompiler;
	public final JavaMethodBytecodeCompiler methodCompiler;
	private final LambdaIndyCompiler lambdaIndyCompiler;
	private final CapturedExpressionVisitor<Void> capturedExpressionVisitor;

	public JavaExpressionVisitor(JavaBytecodeContext context, JavaCompiledModule module, JavaWriter javaWriter, JavaMangler javaMangler) {
		this(context, module, javaWriter, javaMangler, new JavaInvalidCapturedExpressionVisitor());
	}

	public JavaExpressionVisitor(JavaBytecodeContext context, JavaCompiledModule module, JavaWriter javaWriter, JavaMangler javaMangler, CapturedExpressionVisitor<Void> capturedExpressionVisitor) {
		this.javaWriter = javaWriter;
		this.context = context;
		this.module = module;
		this.javaMangler = javaMangler;
		boxingTypeVisitor = JavaBoxingTypeVisitor.forJavaBoxing(javaWriter);
		optionalWrappingTypeVisitor = JavaBoxingTypeVisitor.forOptionalWrapping(javaWriter);
		unboxingTypeVisitor = JavaUnboxingTypeVisitor.forJavaUnboxing(javaWriter);
		optionalUnwrappingTypeVisitor = JavaUnboxingTypeVisitor.forOptionalUnwrapping(javaWriter);
		fieldCompiler = new JavaFieldBytecodeCompiler(javaWriter, this, true);
		methodCompiler = new JavaMethodBytecodeCompiler(javaWriter, this, context, module);
		this.lambdaIndyCompiler = LambdaIndyCompiler.of(this.javaWriter, this.javaMangler, this.context, this.module, this);
		this.capturedExpressionVisitor = capturedExpressionVisitor;
	}

	private static boolean hasNoDefault(MatchExpression switchStatement) {
		for (MatchExpression.Case switchCase : switchStatement.cases)
			if (switchCase.key == null) return false;
		return true;
	}

	@Override
	public Void visitAndAnd(AndAndExpression expression) {
		Label end = new Label();
		Label onFalse = new Label();

		expression.left.accept(this);

		javaWriter.ifEQ(onFalse);
		expression.right.accept(this);

		// //these two calls are redundant but make decompiled code look better. Keep?
		// javaWriter.ifEQ(onFalse);
		// javaWriter.iConst1();

		javaWriter.goTo(end);

		javaWriter.label(onFalse);
		javaWriter.iConst0();


		javaWriter.label(end);

		return null;
	}

	@Override
	public Void visitArray(ArrayExpression expression) {
		Type type = context.getType(expression.arrayType.elementType);
		if (expression.arrayType.elementType.isGeneric()) {
			expression.arrayType.elementType.accept(javaWriter, new JavaTypeExpressionVisitor(context, false));
			javaWriter.constant(expression.expressions.length);
			javaWriter.invokeStatic(ARRAY_NEWINSTANCE);
			javaWriter.checkCast(context.getInternalName(expression.arrayType));
		} else {
			javaWriter.constant(expression.expressions.length);
			javaWriter.newArray(type);
		}
		for (int i = 0; i < expression.expressions.length; i++) {
			javaWriter.dup();
			javaWriter.constant(i);
			expression.expressions[i].accept(this);
			javaWriter.arrayStore(type);
		}
		return null;
	}

	@Override
	public Void visitCompare(CompareExpression expression) {
		if (expression.operator.method instanceof BuiltinMethodSymbol) {
			BuiltinMethodSymbol method = (BuiltinMethodSymbol) expression.operator.method;
			switch (method) {
				case BYTE_COMPARE:
					expression.left.accept(this);
					javaWriter.constant(0xFF);
					javaWriter.iAnd();
					expression.right.accept(this);
					javaWriter.constant(0xFF);
					javaWriter.iAnd();
					compareInt(expression.comparison);
					return null;
				case USHORT_COMPARE:
					expression.left.accept(this);
					javaWriter.constant(0xFFFF);
					javaWriter.iAnd();
					expression.right.accept(this);
					javaWriter.constant(0xFFFF);
					javaWriter.iAnd();
					compareInt(expression.comparison);
					return null;
				case SBYTE_COMPARE:
				case SHORT_COMPARE:
				case INT_COMPARE:
				case CHAR_COMPARE:
				case USIZE_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					compareInt(expression.comparison);
					return null;
				case ULONG_COMPARE_UINT:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.i2l();
					javaWriter.constant(0xFFFF_FFFFL);
					javaWriter.lAnd();
					javaWriter.invokeStatic(JavaBuiltinModule.LONG_COMPARE_UNSIGNED);
					compareGeneric(expression.comparison);
					return null;
				case ULONG_COMPARE_USIZE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.i2l();
					javaWriter.invokeStatic(JavaBuiltinModule.LONG_COMPARE_UNSIGNED);
					compareGeneric(expression.comparison);
					return null;
			}
		}

		JavaMethod method = context.getJavaMethod(expression.operator.method);
		method.compileStatic(methodCompiler, BasicTypeID.INT, new CallArguments(expression.left, expression.right));
		compareGeneric(expression.comparison);
		return null;
	}

	private void compareInt(CompareType comparator) {
		Label exit = new Label();
		Label isTrue = new Label();
		switch (comparator) {
			case EQ:
				javaWriter.ifICmpEQ(isTrue);
				break;
			case NE:
				javaWriter.ifICmpNE(isTrue);
				break;
			case GT:
				javaWriter.ifICmpGT(isTrue);
				break;
			case GE:
				javaWriter.ifICmpGE(isTrue);
				break;
			case LT:
				javaWriter.ifICmpLT(isTrue);
				break;
			case LE:
				javaWriter.ifICmpLE(isTrue);
				break;
			default:
				throw new IllegalStateException("Invalid comparator: " + comparator);
		}
		javaWriter.iConst0();
		javaWriter.goTo(exit);
		javaWriter.label(isTrue);
		javaWriter.iConst1();
		javaWriter.label(exit);
	}

	private void compareGeneric(CompareType comparator) {
		Label exit = new Label();
		Label isTrue = new Label();
		switch (comparator) {
			case EQ:
				javaWriter.ifEQ(isTrue);
				break;
			case NE:
				javaWriter.ifNE(isTrue);
				break;
			case GT:
				javaWriter.ifGT(isTrue);
				break;
			case GE:
				javaWriter.ifGE(isTrue);
				break;
			case LT:
				javaWriter.ifLT(isTrue);
				break;
			case LE:
				javaWriter.ifLE(isTrue);
				break;
			default:
				throw new IllegalStateException("Invalid comparator: " + comparator);
		}
		javaWriter.iConst0();
		javaWriter.goTo(exit);
		javaWriter.label(isTrue);
		javaWriter.iConst1();
		javaWriter.label(exit);
	}

	@Override
	public Void visitCall(CallExpression expression) {
		ModuleSymbol module = expression.method.method.getDefiningType().getModule();
		JavaCompiledModule javaCompiledModule = context.getJavaModule(module);
		JavaMethod method = javaCompiledModule.getMethodInfo(expression.method.method);
		method.compileVirtual(methodCompiler, expression.type, expression.target, expression.arguments);
		if (expression.method.getHeader().getReturnType().isGeneric())
			javaWriter.checkCast(context.getType(expression.type));

		return null;
	}

	@Override
	public Void visitCallStatic(CallStaticExpression expression) {
		JavaMethod method = context.getJavaMethod(expression.member.method);
		if (expression.member.getID().equals(CONSTRUCTOR)) {
			method.compileConstructor(methodCompiler, expression.type, expression.arguments);
		} else {
			method.compileStatic(methodCompiler, expression.type, expression.arguments);
		}
		return null;
	}

	@Override
	public Void visitCallSuper(CallSuperExpression expression) {
		ModuleSymbol module = expression.method.method.getDefiningType().getModule();
		JavaCompiledModule javaCompiledModule = context.getJavaModule(module);
		JavaMethod method = javaCompiledModule.getMethodInfo(expression.method.method);
		method.compileSpecial(methodCompiler, expression.type, expression.target, expression.arguments);
		return null;
	}

	void handleReturnValue(TypeID original, TypeID actual) {
		if (original.isGeneric()) {
			handleGenericReturnValue(actual);
		}
	}

	private void handleGenericReturnValue(TypeID actual) {
		if (CompilerUtils.isPrimitive(actual)) {
			getJavaWriter().checkCast(context.getInternalName(new OptionalTypeID(actual)));
			actual.accept( unboxingTypeVisitor);
		} else {
			Type asmType = Type.getType(context.getType(actual).getDescriptor());
			getJavaWriter().checkCast(asmType);
		}
	}

	@Override
	public Void visitCaptured(CapturedExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	/*
	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		// TODO - does this cover all situations?
		int thisLocal = 0;
		if (javaWriter.forDefinition instanceof ExpansionDefinition) {
			thisLocal = javaWriter.forDefinition.typeParameters.length;
		}
		javaWriter.load(context.getType(expression.type), thisLocal);
		return null;
	}
	 */

	@Override
	public Void visitCheckNull(CheckNullExpression expression) {
		final Label end = new Label();
		expression.value.accept(this);
		javaWriter.dup();
		if (expression.type.withoutOptional() == BasicTypeID.USIZE) {
			javaWriter.iConstM1();
			javaWriter.ifICmpNE(end);
		} else {
			javaWriter.ifNonNull(end);
		}
		javaWriter.pop();
		javaWriter.newObject("java/lang/NullPointerException");
		javaWriter.dup();
		javaWriter.constant("Tried to convert a null value to nonnull type " + context.getType(expression.type).getClassName());
		javaWriter.invokeSpecial(NullPointerException.class, "<init>", "(Ljava/lang/String;)V");
		javaWriter.aThrow();
		javaWriter.label(end);

		expression.type.accept(optionalUnwrappingTypeVisitor);

		return null;
	}

	@Override
	public Void visitCoalesce(CoalesceExpression expression) {
		final Label end = new Label();
		expression.left.accept(this);
		javaWriter.dup();
		javaWriter.ifNonNull(end);
		javaWriter.pop();
		expression.right.accept(this);
		expression.right.type.accept(boxingTypeVisitor);
		javaWriter.label(end);
		expression.type.accept(unboxingTypeVisitor);
		return null;
	}

	@Override
	public Void visitConditional(ConditionalExpression expression) {
		final Label end = new Label();
		final Label onElse = new Label();
		expression.condition.accept(this);
		javaWriter.ifEQ(onElse);
		expression.ifThen.accept(this);
		javaWriter.goTo(end);
		javaWriter.label(onElse);
		expression.ifElse.accept(this);
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitConstantBool(ConstantBoolExpression expression) {
		if (expression.value)
			javaWriter.iConst1();
		else
			javaWriter.iConst0();
		return null;
	}

	@Override
	public Void visitConstantByte(ConstantByteExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantChar(ConstantCharExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantDouble(ConstantDoubleExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantFloat(ConstantFloatExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantInt(ConstantIntExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantLong(ConstantLongExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantSByte(ConstantSByteExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantShort(ConstantShortExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantString(ConstantStringExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUInt(ConstantUIntExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantULong(ConstantULongExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUShort(ConstantUShortExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUSize(ConstantUSizeExpression expression) {
		getJavaWriter().constant((int) expression.value);
		return null;
	}

	@Override
	public Void visitConstructorThisCall(ConstructorThisCallExpression expression) {
		throw new UnsupportedOperationException("Invalid usage");
	}

	@Override
	public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		throw new UnsupportedOperationException("Invalid usage");
	}

	@Override
	public Void visitEnumConstant(EnumConstantExpression expression) {
		javaWriter.getStaticField(context.getInternalName(expression.type), module.getEnumMapper().getMapping(expression.value).orElseGet(() -> expression.value.name), context.getDescriptor(expression.type));
		return null;
	}

	@Override
	public Void visitFunction(FunctionExpression expression) {
		CompilerUtils.tagMethodParameters(context, module, expression.header, false, Collections.emptyList());

        /*if (expression.header.parameters.length == 0 && expression.body instanceof ReturnStatement && expression.body.hasTag(MatchExpression.class) && expression.closure.captures.isEmpty()) {
            ((ReturnStatement) expression.body).value.accept(this);
            return null;
        }*/

		final String interfaceName;
		if (expression.type instanceof JavaFunctionalInterfaceTypeID) {
			//Let's implement the functional Interface instead
			JavaFunctionalInterfaceTypeID type = (JavaFunctionalInterfaceTypeID) expression.type;
			//Should be the same, should it not?
			interfaceName = Type.getInternalName(type.functionalInterfaceMethod.getDeclaringClass());
		} else {
			//Normal way, no casting to functional interface
			FunctionHeader header = expression.original == null ? expression.header : expression.original;
			interfaceName = context.getInternalName(new FunctionTypeID(header));
		}

		final JavaNativeMethod functionalMethod = context.getFunctionalInterface(expression.original == null ? expression.type : new FunctionTypeID(expression.original));
		final JavaNativeMethod methodInfo = functionalMethod.withModifiers(functionalMethod.modifiers & ~JavaModifiers.ABSTRACT);

		this.lambdaIndyCompiler.compileFunctionExpressionViaIndy(expression, interfaceName, methodInfo);

		return null;
	}

	@Override
	public Void visitGenericCast(GenericWildcardCastExpression expression) {
		return expression.value.accept(this);
	}

	@Override
	public Void visitGetField(GetFieldExpression expression) {
		JavaField field = context.getJavaField(expression.field);
		field.compileInstanceGet(fieldCompiler, expression.target);
		handleReturnValue(expression.field.field.getType(), expression.field.getType());
		return null;
	}

	@Override
	public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		JavaParameterInfo parameter = module.getParameterInfo(expression.parameter);

		if (parameter == null)
			throw new RuntimeException(expression.position.toString() + ": Could not resolve lambda parameter" + expression.parameter);

		javaWriter.load(context.getType(expression.parameter.type), parameter.index);
		return null;
	}

	@Override
	public Void visitGetLocalVariable(GetLocalVariableExpression expression) {
		return tagVariableAndUpdateLastUsage(expression.variable.id);
	}

	@Override
	public Void visitGetMatchingVariantField(GetMatchingVariantField expression) {
		javaWriter.loadObject(0);
		final TypeID type = expression.value.option.getParameterType(expression.index);
		final JavaVariantOption tag = context.getJavaVariantOption(expression.value.option);
		javaWriter.checkCast(tag.variantOptionClass.internalName);
		javaWriter.getField(new JavaNativeField(tag.variantOptionClass, "field" + expression.index, context.getDescriptor(type)));
		return null;
	}

	@Override
	public Void visitGetStaticField(GetStaticFieldExpression expression) {
		context.getJavaField(expression.field).compileStaticGet(fieldCompiler);
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
		javaWriter.checkCast(context.getInternalName(expression.type));
		return null;
	}

	@Override
	public Void visitIs(IsExpression expression) {
		expression.value.accept(this);
		javaWriter.instanceOf(context.getInternalName(expression.isType));
		return null;
	}

	@Override
	public Void visitMakeConst(MakeConstExpression expression) {
		return null;
	}

	@Override
	public Void visitMap(MapExpression expression) {
		javaWriter.newObject("java/util/HashMap");
		javaWriter.dup();
		javaWriter.invokeSpecial("java/util/HashMap", "<init>", "()V");
		final AssocTypeID type = (AssocTypeID) expression.type;
		for (int i = 0; i < expression.keys.length; i++) {
			javaWriter.dup();
			expression.keys[i].accept(this);
			type.keyType.accept(boxingTypeVisitor);
			expression.values[i].accept(this);
			type.valueType.accept(boxingTypeVisitor);
			javaWriter.invokeInterface(MAP_PUT);
			javaWriter.pop();
		}
		return null;
	}

	@Override
	public Void visitMatch(MatchExpression expression) {
		final Label start = new Label();
		final Label end = new Label();
		final boolean isVariantOptionSwitch = expression.value.type.isVariant();

		javaWriter.label(start);
		expression.value.accept(this);

		if (isVariantOptionSwitch) {
			// For Variant options, we need to access the switched value inside the switch, so we dup here
			//  We need the Option so that we can destruct the variant into its components.
			// this value will be popped at the JavaSwitchKeyVariableVisitor
			javaWriter.dup(context.getType(expression.value.type));
		}

		//TODO replace beforeSwitch visitor or similar
		if (expression.value.type == BasicTypeID.STRING)
			javaWriter.invokeVirtual(OBJECT_HASHCODE);

		//TODO replace with beforeSwitch visitor or similar
		if (isVariantOptionSwitch) {
			JavaClass cls = context.getJavaClass(expression.value.type.asDefinition().get().definition);
			javaWriter.invokeVirtual(JavaNativeMethod.getNativeVirtual(cls, "getDenominator", "()I"));
		}

		final boolean hasNoDefault = hasNoDefault(expression);

		final MatchExpression.Case[] cases = expression.cases;
		final JavaSwitchLabel[] switchLabels = new JavaSwitchLabel[hasNoDefault ? cases.length : cases.length - 1];
		final Label defaultLabel = new Label();

		int i = 0;
		for (final MatchExpression.Case matchCase : cases) {
			if (matchCase.key != null) {
				switchLabels[i++] = new JavaSwitchLabel(CompilerUtils.getKeyForSwitch(matchCase.key), new Label());
			}
		}

		JavaSwitchLabel[] sortedSwitchLabels = Arrays.copyOf(switchLabels, switchLabels.length);
		Arrays.sort(sortedSwitchLabels, Comparator.comparingInt(a -> a.key));

		javaWriter.lookupSwitch(defaultLabel, sortedSwitchLabels);

		i = 0;
		for (final MatchExpression.Case switchCase : cases) {
			final Label caseStart = new Label();
			final Label caseEnd = new Label();

			if (hasNoDefault || switchCase.key != null) {
				javaWriter.label(switchLabels[i++].label);
			} else {
				javaWriter.label(defaultLabel);
			}
			javaWriter.label(caseStart);

			// switchCase.key == null => default case
			if (switchCase.key != null) {
				switchCase.key.accept(new JavaSwitchKeyVariableVisitor(javaWriter, context, caseStart, caseEnd));
			}
			if (isVariantOptionSwitch) {
				javaWriter.pop();
			}

			switchCase.value.accept(this);
			javaWriter.label(caseEnd);
			javaWriter.goTo(end);
		}

		if (hasNoDefault) {
			// this can only occur if the switch was deemed exhaustive by the validator,
			// so ending up here is always an error
			javaWriter.label(defaultLabel);
			if (isVariantOptionSwitch) {
				javaWriter.pop();
			}

			javaWriter.newObject("java/lang/AssertionError");
			javaWriter.dup();
			javaWriter.constant("Reached default case on an exhaustive switch");
			javaWriter.invokeSpecial(AssertionError.class, "<init>", "(Ljava/lang/Object;)V");
			javaWriter.aThrow();
		}

		javaWriter.label(end);

		// TODO: what's this one exactly for?
		if (!CompilerUtils.isPrimitive(expression.type)) {
			javaWriter.checkCast(context.getType(expression.type));
		}
		return null;
	}

	@Override
	public Void visitNull(NullExpression expression) {
		if (expression.type != BasicTypeID.NULL && expression.type.withoutOptional() == BasicTypeID.USIZE) {
			javaWriter.constant(-1); // special case: usize? null = -1
		} else {
			javaWriter.aConstNull();
		}
		return null;
	}

	@Override
	public Void visitOrOr(OrOrExpression expression) {
		Label end = new Label();
		Label onTrue = new Label();

		expression.left.accept(this);

		javaWriter.ifNE(onTrue);
		expression.right.accept(this);

		// //these two calls are redundant but make decompiled code look better. Keep?
		// javaWriter.ifNE(onTrue);
		// javaWriter.iConst0();

		javaWriter.goTo(end);

		javaWriter.label(onTrue);
		javaWriter.iConst1();


		javaWriter.label(end);

		return null;
	}

	@Override
	public Void visitPanic(PanicExpression expression) {
		javaWriter.newObject("java/lang/AssertionError");
		javaWriter.dup();
		expression.value.accept(this);
		javaWriter.invokeSpecial(AssertionError.class, "<init>", "(Ljava/lang/Object;)V");
		javaWriter.aThrow();
		return null;
	}

	private void modify(ModifiableExpression source, BuiltinMethodSymbol builtin, PushOption pushOption) {
		switch (builtin) {
			case BYTE_INC:
				modify(source, () -> {
					javaWriter.iConst1();
					javaWriter.iAdd();
					javaWriter.constant(255);
					javaWriter.iAnd();
				}, pushOption);
				return;
			case BYTE_DEC:
				modify(source, () -> {
					javaWriter.iConst1();
					javaWriter.iSub();
					javaWriter.constant(255);
					javaWriter.iAnd();
				}, pushOption);
				return;
			case SBYTE_INC:
				modify(source, () -> {
					javaWriter.iConst1();
					javaWriter.iAdd();
					javaWriter.i2b();
				}, pushOption);
				return;
			case SBYTE_DEC:
				modify(source, () -> {
					javaWriter.iConst1();
					javaWriter.iSub();
					javaWriter.i2b();
				}, pushOption);
				return;
			case SHORT_INC:
				modify(source, () -> {
					javaWriter.iConst1();
					javaWriter.iAdd();
					javaWriter.i2s();
				}, pushOption);
				return;
			case SHORT_DEC:
				modify(source, () -> {
					javaWriter.iConst1();
					javaWriter.iSub();
					javaWriter.i2s();
				}, pushOption);
				return;
			case USHORT_INC:
				modify(source, () -> {
					javaWriter.iConst1();
					javaWriter.iAdd();
					javaWriter.constant(0xFFFF);
					javaWriter.iAnd();
				}, pushOption);
				return;
			case USHORT_DEC:
				modify(source, () -> {
					javaWriter.iConst1();
					javaWriter.iSub();
					javaWriter.constant(0xFFFF);
					javaWriter.iAnd();
				}, pushOption);
				return;
			case INT_INC:
			case UINT_INC:
			case USIZE_INC:
				if (source instanceof GetLocalVariableExpression) {
					JavaLocalVariableInfo local = javaWriter.getLocalVariable(((GetLocalVariableExpression) source).variable.id);
					if (pushOption == PushOption.BEFORE) {
						javaWriter.load(local);
					}
					javaWriter.iinc(local.local);
					if (pushOption == PushOption.AFTER) {
						javaWriter.load(local);
					}
				} else {
					modify(source, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
					}, pushOption);
				}
				return;
			case INT_DEC:
			case UINT_DEC:
			case USIZE_DEC:
				if (source instanceof GetLocalVariableExpression) {
					JavaLocalVariableInfo local = javaWriter.getLocalVariable(((GetLocalVariableExpression) source).variable.id);
					if (pushOption == PushOption.BEFORE) {
						javaWriter.load(local);
					}
					javaWriter.idec(local.local);
					if (pushOption == PushOption.AFTER) {
						javaWriter.load(local);
					}

				} else {
					modify(source, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
					}, pushOption);
				}
				return;
			case LONG_INC:
			case ULONG_INC:
				modify(source, () -> {
					javaWriter.constant(1L);
					javaWriter.lAdd();
				}, pushOption);
				return;
			case LONG_DEC:
			case ULONG_DEC:
				modify(source, () -> {
					javaWriter.constant(1L);
					javaWriter.lSub();
				}, pushOption);
				return;
			case FLOAT_INC:
				modify(source, () -> {
					javaWriter.constant(1f);
					javaWriter.fAdd();
				}, pushOption);
				return;
			case FLOAT_DEC:
				modify(source, () -> {
					javaWriter.constant(1f);
					javaWriter.fSub();
				}, pushOption);
				return;
			case DOUBLE_INC:
				modify(source, () -> {
					javaWriter.constant(1d);
					javaWriter.dAdd();
				}, pushOption);
				return;
			case DOUBLE_DEC:
				modify(source, () -> {
					javaWriter.constant(1d);
					javaWriter.dSub();
				}, pushOption);
				return;
			default:
				throw new IllegalArgumentException("Unknown builtin: " + builtin);
		}
	}

	private void modify(ModifiableExpression source, Runnable modification, PushOption push) {
		source.accept(new JavaModificationExpressionVisitor(context, module, javaWriter, this, modification, push));
	}

	@Override
	public Void visitPlatformSpecific(Expression expression) {
		if (!(expression instanceof JavaFunctionInterfaceCastExpression)) {
			throw new AssertionError("Unrecognized platform expression " + expression.getClass().getName() + ": " + expression);
		}

		final JavaFunctionInterfaceCastExpression jficExpression = (JavaFunctionInterfaceCastExpression) expression;

		if (jficExpression.value.type instanceof JavaFunctionalInterfaceTypeID) {
			return jficExpression.value.accept(this);
		}

		this.lambdaIndyCompiler.convertTypeOfFunctionExpressionViaIndy(jficExpression);
		return null;
	}

	private PushOption getPushOption(ModificationExpression.Modification modification) {
		switch (modification) {
			case PostDecrement:
			case PostIncrement:
				return PushOption.BEFORE;
			case PreDecrement:
			case PreIncrement:
				return PushOption.AFTER;
			default:
				throw new IllegalArgumentException("Unknown modification: " + modification);
		}
	}

	@Override
	public Void visitModification(ModificationExpression expression) {
		modify(expression, getPushOption(expression.modification));
		return null;
	}

	public void modify(ModificationExpression expression, PushOption pushOption) {
		if (expression.method.method instanceof BuiltinMethodSymbol) {
			BuiltinMethodSymbol builtin = (BuiltinMethodSymbol) expression.method.method;
			modify(expression.target, builtin, pushOption);
		} else {
			modify(
					expression.target,
					() -> context.getJavaMethod(expression.method).compileVirtualWithTargetOnTopOfStack(methodCompiler, expression.type, CallArguments.EMPTY),
					pushOption
			);
		}
	}

	@Override
	public Void visitRange(RangeExpression expression) {
		RangeTypeID type = (RangeTypeID) expression.type;
		Type cls = context.getType(expression.type);
		javaWriter.newObject(cls.getInternalName());
		javaWriter.dup();
		expression.from.accept(this);
		expression.to.accept(this);
		javaWriter.invokeSpecial(cls.getInternalName(), "<init>", "(" + context.getDescriptor(type.baseType) + context.getDescriptor(type.baseType) + ")V");

		return null;
	}


	@Override
	public Void visitSameObject(SameObjectExpression expression) {
		expression.left.accept(this);
		expression.right.accept(this);

		Label end = new Label();
		Label equal = new Label();

		if (expression.inverted)
			javaWriter.ifACmpNe(equal);
		else
			javaWriter.ifACmpEq(equal);

		javaWriter.iConst0();
		javaWriter.goTo(end);
		javaWriter.label(equal);
		javaWriter.iConst1();
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitSetField(SetFieldExpression expression) {
		context.getJavaField(expression.field).compileInstanceSet(fieldCompiler, expression.target, expression.value);
		return null;
	}

	@Override
	public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		expression.value.accept(this);
		javaWriter.dup(context.getType(expression.value.type));
		JavaParameterInfo parameter = module.getParameterInfo(expression.parameter);
		javaWriter.store(context.getType(expression.type), parameter.index);
		return null;
	}

	@Override
	public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
		expression.value.accept(this);
		Label label = new Label();
		javaWriter.label(label);
		final JavaLocalVariableInfo tag = javaWriter.getLocalVariable(expression.variable.id);
		tag.end = label;

		javaWriter.dup(context.getType(expression.value.type));
		javaWriter.store(tag.type, tag.local);
		return null;
	}

	@Override
	public Void visitSetStaticField(SetStaticFieldExpression expression) {
		context.getJavaField(expression.field).compileStaticSet(fieldCompiler, expression.value);
		return null;
	}

	@Override
	public Void visitSupertypeCast(SupertypeCastExpression expression) {
		expression.value.accept(this);
		javaWriter.checkCast(context.getType(expression.type));
		return null; // nothing to do
	}

	@Override
	public Void visitSubtypeCast(SubtypeCastExpression expression) {
		expression.value.accept(this);
		javaWriter.checkCast(context.getType(expression.type));
		return null; // nothing to do
	}

	@Override
	public Void visitThis(ThisExpression expression) {
		int thisLocal = 0;
		if (javaWriter.forDefinition instanceof ExpansionDefinition) {
			thisLocal = javaWriter.forDefinition.typeParameters.length;
		}
		javaWriter.load(context.getType(expression.type), thisLocal);
		return null;
	}

	@Override
	public Void visitThrow(ThrowExpression expression) {
		expression.value.accept(this);
		javaWriter.aThrow();
		return null;
	}

	@Override
	public Void visitTryConvert(TryConvertExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
		expression.value.accept(this);
		javaWriter.dup();
		//FIXME better way of finding the error
		final String internalName = context.getInternalName(expression.value.type) + "$Error";
		javaWriter.instanceOf(internalName);
		final Label end = new Label();
		javaWriter.ifNE(end);
		javaWriter.newObject(Type.getInternalName(Exception.class));
		javaWriter.dup();
		javaWriter.invokeSpecial(Type.getInternalName(Exception.class), "<init>", "()V");
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
		expression.value.accept(this);
		javaWriter.dup();
		//FIXME better way of finding the error
		final String internalName = context.getInternalName(expression.value.type) + "$Error";
		javaWriter.instanceOf(internalName);
		final Label end = new Label();
		javaWriter.ifNE(end);
		javaWriter.newObject(internalName);
		javaWriter.dupX1();
		javaWriter.swap();
		javaWriter.invokeSpecial(internalName, "<init>", "(Ljava/lang/Object;)V");
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitVariantValue(VariantValueExpression expression) {
		JavaVariantOption tag = context.getJavaVariantOption(expression.option);
		final String internalName = tag.variantOptionClass.internalName;
		javaWriter.newObject(internalName);
		javaWriter.dup();

		for (Expression argument : expression.arguments) {
			argument.accept(this);
		}

		final StringBuilder builder = new StringBuilder("(");
		for (TypeID type : expression.option.getOption().types) {
			builder.append(context.getDescriptor(type));
		}
		builder.append(")V");


		javaWriter.invokeSpecial(internalName, "<init>", builder.toString());
		return null;
	}

	@Override
	public Void visitWrapOptional(WrapOptionalExpression expression) {
		//Does nothing if not required to be wrapped
		expression.value.accept(this);
		expression.value.type.accept(optionalWrappingTypeVisitor);
		return null;
	}

	@Override
	public Void visitMemoized(MemoizedExpression expression) {
		if (expression.hasVariableID()) {
			// We already created a temp variable for this -> reuse it
			return tagVariableAndUpdateLastUsage(expression.getVariableID());
		}

		// first call evaluates it and stores it in a local variable called "memoized"
		VariableID newVariable = new VariableID();
		expression.setVariableID(newVariable);

		expression.target.accept(this);
		Type asmType = context.getType(expression.type);
		int local = javaWriter.local(asmType);

		Label start = new Label();
		javaWriter.label(start);
		JavaLocalVariableInfo memoized = new JavaLocalVariableInfo(asmType, local, start, "memoized");
		javaWriter.setLocalVariable(newVariable, memoized);
		javaWriter.addVariableInfo(memoized);

		javaWriter.dup(asmType);
		javaWriter.store(asmType, local);
		return null;
	}

	private Void tagVariableAndUpdateLastUsage(VariableID id) {
		final Label label = new Label();
		final JavaLocalVariableInfo tag = javaWriter.getLocalVariable(id);

		tag.end = label;
		javaWriter.load(tag.type, tag.local);
		javaWriter.label(label);
		return null;
	}

	public JavaWriter getJavaWriter() {
		return javaWriter;
	}
}
