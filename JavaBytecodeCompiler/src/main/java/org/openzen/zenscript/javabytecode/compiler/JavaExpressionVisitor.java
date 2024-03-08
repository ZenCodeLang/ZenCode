package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javabytecode.compiler.JavaModificationExpressionVisitor.PushOption;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;
import org.openzen.zenscript.javashared.expressions.JavaFunctionInterfaceCastExpression;
import org.openzen.zenscript.javashared.types.JavaFunctionalInterfaceTypeID;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.openzen.zenscript.javabytecode.compiler.JavaMethodBytecodeCompiler.OBJECT_HASHCODE;

public class JavaExpressionVisitor implements ExpressionVisitor<Void> {
	private static final JavaNativeMethod MAP_PUT = JavaNativeMethod.getInterface(JavaClass.MAP, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	private static final JavaNativeMethod ARRAY_NEWINSTANCE = JavaNativeMethod.getNativeStatic(JavaClass.ARRAY, "newInstance", "(Ljava/lang/Class;I)Ljava/lang/Object;");
	private static final MethodID CONSTRUCTOR = MethodID.staticOperator(OperatorType.CONSTRUCTOR);

	final JavaWriter javaWriter;
	final JavaBytecodeContext context;
	final JavaCompiledModule module;
	private final JavaBoxingTypeVisitor boxingTypeVisitor;
	private final JavaUnboxingTypeVisitor unboxingTypeVisitor;
	private final JavaCapturedExpressionVisitor capturedExpressionVisitor = new JavaCapturedExpressionVisitor(this);
	private final JavaFieldBytecodeCompiler fieldCompiler;
	private final JavaMethodBytecodeCompiler methodCompiler;

	public JavaExpressionVisitor(JavaBytecodeContext context, JavaCompiledModule module, JavaWriter javaWriter) {
		this.javaWriter = javaWriter;
		this.context = context;
		this.module = module;
		boxingTypeVisitor = new JavaBoxingTypeVisitor(javaWriter);
		unboxingTypeVisitor = new JavaUnboxingTypeVisitor(javaWriter);
		fieldCompiler = new JavaFieldBytecodeCompiler(javaWriter, this, true);
		methodCompiler = new JavaMethodBytecodeCompiler(javaWriter, this, context, module);
	}

	//TODO replace with visitor?
	private static int calculateMemberPosition(GetLocalVariableExpression localVariableExpression, FunctionExpression expression) {
		int h = 1;//expression.header.parameters.length;
		for (CapturedExpression capture : expression.closure.captures) {
			if (capture instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) capture).variable == localVariableExpression.variable)
				return h;
			if (capture instanceof CapturedClosureExpression && ((CapturedClosureExpression) capture).value instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) ((CapturedClosureExpression) capture).value).variable == localVariableExpression.variable)
				return h;
			h++;
		}
		throw new RuntimeException(localVariableExpression.position.toString() + ": Captured Statement error");
	}

	private static int calculateMemberPosition(CapturedParameterExpression functionParameterExpression, FunctionExpression expression) {
		int h = 1;//expression.header.parameters.length;
		for (CapturedExpression capture : expression.closure.captures) {
			if (capture instanceof CapturedParameterExpression && ((CapturedParameterExpression) capture).parameter == functionParameterExpression.parameter)
				return h;
			h++;
		}
		throw new RuntimeException(functionParameterExpression.position.toString() + ": Captured Statement error");
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

			expression.arrayType.elementType.accept(javaWriter, new JavaTypeExpressionVisitor(context));
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

	private void handleReturnValue(TypeID original, TypeID actual) {
		if (original.isGeneric()) {
			handleGenericReturnValue(actual);
		}
	}

	private void handleGenericReturnValue(TypeID actual) {
		if (CompilerUtils.isPrimitive(actual)) {
			getJavaWriter().checkCast(context.getInternalName(new OptionalTypeID(actual)));
			actual.accept(actual, unboxingTypeVisitor);
		} else {
			getJavaWriter().checkCast(context.getInternalName(actual));
		}
	}

	@Override
	public Void visitCapturedClosure(CapturedClosureExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCheckNull(CheckNullExpression expression) {
		final Label end = new Label();
		expression.value.accept(this);
		javaWriter.dup();
		javaWriter.ifNonNull(end);
		javaWriter.pop();
		javaWriter.newObject("java/lang/NullPointerException");
		javaWriter.dup();
		javaWriter.constant("Tried to convert a null value to nonnull type " + context.getType(expression.type).getClassName());
		javaWriter.invokeSpecial(NullPointerException.class, "<init>", "(Ljava/lang/String;)V");
		javaWriter.aThrow();
		javaWriter.label(end);

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
		expression.right.type.accept(expression.right.type, boxingTypeVisitor);
		javaWriter.label(end);
		expression.type.accept(expression.type, unboxingTypeVisitor);
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
		getJavaWriter().siPush(expression.value);
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

		final String signature;
		final String[] interfaces;
		final String descriptor;

		{//Fill the info above
			if (expression.type instanceof JavaFunctionalInterfaceTypeID) {
				//Let's implement the functional Interface instead
				JavaFunctionalInterfaceTypeID type = (JavaFunctionalInterfaceTypeID) expression.type;
				final Method functionalInterfaceMethod = type.functionalInterfaceMethod;

				//Should be the same, should it not?
				signature = context.getMethodSignature(expression.header, true);
				descriptor = context.getMethodDescriptor(expression.header);
				interfaces = new String[]{Type.getInternalName(functionalInterfaceMethod.getDeclaringClass())};
			} else {
				//Normal way, no casting to functional interface
				signature = context.getMethodSignature(expression.header, true);
				descriptor = context.getMethodDescriptor(expression.header);
				interfaces = new String[]{context.getInternalName(new FunctionTypeID(expression.header))};
			}
		}

		final JavaNativeMethod methodInfo;
		// We don't allow registering classes starting with "java"
		final String className = interfaces[0].replace("java", "j").replace("/", "_") + "_" + context.getLambdaCounter();
		{
			final JavaNativeMethod m = context.getFunctionalInterface(expression.type);
			methodInfo = new JavaNativeMethod(m.cls, m.kind, m.name, m.compile, m.descriptor, m.modifiers & ~JavaModifiers.ABSTRACT, m.genericResult, m.typeParameterArguments);
		}
		final ClassWriter lambdaCW = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
		JavaClass lambdaClass = JavaClass.fromInternalName(className, JavaClass.Kind.CLASS);
		lambdaCW.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", interfaces);
		final JavaWriter functionWriter;

		//Bridge method!!!
		if (!Objects.equals(methodInfo.descriptor, descriptor)) {
			final JavaNativeMethod bridgeMethodInfo = new JavaNativeMethod(methodInfo.cls, methodInfo.kind, methodInfo.name, methodInfo.compile, methodInfo.descriptor, methodInfo.modifiers | JavaModifiers.BRIDGE | JavaModifiers.SYNTHETIC, methodInfo.genericResult, methodInfo.typeParameterArguments);
			JavaCompilingMethod compilingBridgeMethod = new JavaCompilingMethod(javaWriter.method.class_, bridgeMethodInfo, signature);
			final JavaWriter bridgeWriter = new JavaWriter(context.logger, expression.position, lambdaCW, compilingBridgeMethod, null);
			bridgeWriter.start();

			//This.name(parameters, casted)
			bridgeWriter.loadObject(0);

			for (int i = 0; i < expression.header.parameters.length; i++) {
				final FunctionParameter functionParameter = expression.header.parameters[i];
				final Type type = context.getType(functionParameter.type);
				bridgeWriter.load(type, i + 1);
				if (!CompilerUtils.isPrimitive(functionParameter.type)) {
					bridgeWriter.checkCast(type);
				}
			}

			bridgeWriter.invokeVirtual(new JavaNativeMethod(JavaClass.fromInternalName(className, JavaClass.Kind.CLASS), JavaNativeMethod.Kind.INSTANCE, methodInfo.name, methodInfo.compile, descriptor, methodInfo.modifiers, methodInfo.genericResult));
			final TypeID returnType = expression.header.getReturnType();
			if (returnType != BasicTypeID.VOID) {
				final Type returnTypeASM = context.getType(returnType);
				if (!CompilerUtils.isPrimitive(returnType)) {
					bridgeWriter.checkCast(returnTypeASM);
				}
				bridgeWriter.returnType(returnTypeASM);
			}

			bridgeWriter.ret();
			bridgeWriter.end();

			JavaNativeMethod actualMethod = methodInfo.createBridge(context.getMethodDescriptor(expression.header));
			JavaCompilingMethod actualCompiling = new JavaCompilingMethod(lambdaClass, actualMethod, signature);
			//No @Override
			functionWriter = new JavaWriter(context.logger, expression.position, lambdaCW, actualCompiling, null);
		} else {
			JavaCompilingMethod actualCompiling = new JavaCompilingMethod(lambdaClass, methodInfo, signature);
			functionWriter = new JavaWriter(context.logger, expression.position, lambdaCW, actualCompiling, null);
		}

		javaWriter.newObject(className);
		javaWriter.dup();

		// ToDo: Is this fine or do we need the actual signature here?
		//   To check: write a test where the ctor desc and signature would differ and make sure the program compiles/executes
		final String constructorDescriptorAndSignature = calcFunctionDescriptor(expression.closure);
		JavaNativeMethod constructor = JavaNativeMethod.getConstructor(lambdaClass, constructorDescriptorAndSignature, Opcodes.ACC_PUBLIC);
		JavaCompilingMethod constructorCompiling = new JavaCompilingMethod(lambdaClass, constructor, constructorDescriptorAndSignature);
		final JavaWriter constructorWriter = new JavaWriter(context.logger, expression.position, lambdaCW, constructorCompiling, null);
		constructorWriter.start();
		constructorWriter.loadObject(0);
		constructorWriter.dup();
		constructorWriter.invokeSpecial(Object.class, "<init>", "()V");

		int i = 0;
		for (CapturedExpression capture : expression.closure.captures) {
			constructorWriter.dup();
			Type type = context.getType(capture.type);
			lambdaCW.visitField(Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE, "captured" + ++i, type.getDescriptor(), null, null).visitEnd();

			capture.accept(this);

			constructorWriter.load(type, i);
			constructorWriter.putField(className, "captured" + i, type.getDescriptor());
		}

		constructorWriter.pop();

		javaWriter.invokeSpecial(className, "<init>", constructorDescriptorAndSignature);

		constructorWriter.ret();
		constructorWriter.end();


		functionWriter.start();


		final JavaStatementVisitor CSV = new JavaStatementVisitor(context, new JavaExpressionVisitor(context, module, functionWriter) {
			@Override
			public Void visitGetLocalVariable(GetLocalVariableExpression varExpression) {
				final JavaLocalVariableInfo localVariable = functionWriter.tryGetLocalVariable(varExpression.variable.id);
				if (localVariable != null) {
					final Label label = new Label();
					localVariable.end = label;
					functionWriter.label(label);
					functionWriter.load(localVariable);
					return null;
				}


				final int position = calculateMemberPosition(varExpression, expression);
				functionWriter.loadObject(0);
				functionWriter.getField(className, "captured" + position, context.getDescriptor(varExpression.variable.type));
				return null;
			}

			@Override
			public Void visitCapturedParameter(CapturedParameterExpression varExpression) {
				final int position = calculateMemberPosition(varExpression, expression);
				functionWriter.loadObject(0);
				functionWriter.getField(className, "captured" + position, context.getDescriptor(varExpression.parameter.type));
				return null;
			}
		});

		expression.body.accept(CSV);

		functionWriter.ret();
		functionWriter.end();
		lambdaCW.visitEnd();

		context.register(className, lambdaCW.toByteArray());

		return null;
	}

	private String calcFunctionDescriptor(LambdaClosure closure) {
		StringJoiner joiner = new StringJoiner("", "(", ")V");
		for (CapturedExpression capture : closure.captures) {
			String descriptor = context.getDescriptor(capture.type);
			joiner.add(descriptor);
		}
		return joiner.toString();
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
		final Label label = new Label();
		final JavaLocalVariableInfo tag = javaWriter.getLocalVariable(expression.variable.id);

		tag.end = label;
		javaWriter.load(tag.type, tag.local);
		javaWriter.label(label);
		return null;
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
			type.keyType.accept(type.keyType, boxingTypeVisitor);
			expression.values[i].accept(this);
			type.valueType.accept(type.valueType, boxingTypeVisitor);
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
			if(switchCase.key != null) {
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

	private void modify(Expression source, Runnable modification, PushOption push) {
		source.accept(new JavaModificationExpressionVisitor(context, module, javaWriter, this, modification, push));
	}

	@Override
	public Void visitPlatformSpecific(Expression expression) {
		if (expression instanceof JavaFunctionInterfaceCastExpression) {
			JavaFunctionInterfaceCastExpression jficExpression = (JavaFunctionInterfaceCastExpression) expression;
			if (jficExpression.value.type instanceof JavaFunctionalInterfaceTypeID) {
				jficExpression.value.accept(this);
			} else {
				visitFunctionalInterfaceWrapping(jficExpression);
			}
		} else {
			throw new AssertionError("Unrecognized platform expression: " + expression);
		}
		return null;
	}

	@Override
	public Void visitPostCall(PostCallExpression expression) {
		if (expression.member.method instanceof BuiltinMethodSymbol) {
			BuiltinMethodSymbol builtin = (BuiltinMethodSymbol) expression.member.method;
			switch (builtin) {
				case BYTE_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.constant(255);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case BYTE_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.constant(255);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case SBYTE_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.i2b();
					}, PushOption.BEFORE);
					return null;
				case SBYTE_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.i2b();
					}, PushOption.BEFORE);
					return null;
				case SHORT_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.i2s();
					}, PushOption.BEFORE);
					return null;
				case SHORT_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.i2s();
					}, PushOption.BEFORE);
					return null;
				case USHORT_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.constant(0xFFFF);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case USHORT_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.constant(0xFFFF);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case INT_INC:
				case UINT_INC:
				case USIZE_INC:
					if (expression.target instanceof GetLocalVariableExpression) {
						JavaLocalVariableInfo local = javaWriter.getLocalVariable(((GetLocalVariableExpression) expression.target).variable.id);
						javaWriter.load(local);
						javaWriter.iinc(local.local);
					} else {
						modify(expression.target, () -> {
							javaWriter.iConst1();
							javaWriter.iAdd();
						}, PushOption.BEFORE);
					}
					return null;
				case INT_DEC:
				case UINT_DEC:
				case USIZE_DEC:
					if (expression.target instanceof GetLocalVariableExpression) {
						JavaLocalVariableInfo local = javaWriter.getLocalVariable(((GetLocalVariableExpression) expression.target).variable.id);
						javaWriter.load(local);
						javaWriter.iinc(local.local, -1);
					} else {
						modify(expression.target, () -> {
							javaWriter.iConst1();
							javaWriter.iSub();
						}, PushOption.BEFORE);
					}
					return null;
				case LONG_INC:
				case ULONG_INC:
					modify(expression.target, () -> {
						javaWriter.constant(1L);
						javaWriter.lAdd();
					}, PushOption.BEFORE);
					return null;
				case LONG_DEC:
				case ULONG_DEC:
					modify(expression.target, () -> {
						javaWriter.constant(1L);
						javaWriter.lSub();
					}, PushOption.BEFORE);
					return null;
				case FLOAT_INC:
					modify(expression.target, () -> {
						javaWriter.constant(1f);
						javaWriter.fAdd();
					}, PushOption.BEFORE);
					return null;
				case FLOAT_DEC:
					modify(expression.target, () -> {
						javaWriter.constant(1f);
						javaWriter.fSub();
					}, PushOption.BEFORE);
					return null;
				case DOUBLE_INC:
					modify(expression.target, () -> {
						javaWriter.constant(1d);
						javaWriter.dAdd();
					}, PushOption.BEFORE);
					return null;
				case DOUBLE_DEC:
					modify(expression.target, () -> {
						javaWriter.constant(1d);
						javaWriter.dSub();
					}, PushOption.BEFORE);
					return null;
				default:
					throw new IllegalArgumentException("Unknown postcall builtin: " + builtin);
			}
		}

		modify(expression.target, () -> {
			context.getJavaMethod(expression.member).compileVirtual(methodCompiler, expression.type, expression, CallArguments.EMPTY);
		}, PushOption.BEFORE);

		return null;
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

	private void visitFunctionalInterfaceWrapping(JavaFunctionInterfaceCastExpression expression) {
		final FunctionCastWrapperClass wrapper = generateFunctionCastWrapperClass(
				expression.position,
				(FunctionTypeID) expression.value.type,
				expression.functionType);

		expression.value.accept(this);
		javaWriter.newObject(wrapper.className);
		javaWriter.dupX1();
		javaWriter.swap();
		javaWriter.invokeSpecial(wrapper.className, "<init>", wrapper.constructorDesc);
	}

	private FunctionCastWrapperClass generateFunctionCastWrapperClass(CodePosition position, FunctionTypeID fromType, FunctionTypeID toType) {
		final String lambdaName = "lambda" + context.getLambdaCounter();
		final JavaClass classInfo = new JavaClass("zsynthetic", lambdaName, JavaClass.Kind.CLASS);
		final String className = "zsynthetic/" + lambdaName;

		String[] interfaces;
		String wrappedFromSignature = context.getDescriptor(fromType);
		String methodDescriptor;
		String methodSignature;
		Type[] methodParameterTypes;
		JavaNativeMethod implementationMethod;
		if (toType instanceof JavaFunctionalInterfaceTypeID) {
			JavaNativeMethod javaMethod = ((JavaFunctionalInterfaceTypeID) toType).method;
			implementationMethod = new JavaNativeMethod(
					classInfo,
					JavaNativeMethod.Kind.COMPILED,
					javaMethod.name,
					true,
					javaMethod.descriptor,
					javaMethod.modifiers & ~JavaModifiers.ABSTRACT,
					javaMethod.genericResult,
					javaMethod.typeParameterArguments);

			final Method functionalInterfaceMethod = ((JavaFunctionalInterfaceTypeID) toType).functionalInterfaceMethod;

			methodDescriptor = Type.getMethodDescriptor(functionalInterfaceMethod);
			// ToDo: Is signature===descriptor fine here or do we need the actual signature here?
			methodSignature = methodDescriptor;
			interfaces = new String[]{Type.getInternalName(functionalInterfaceMethod.getDeclaringClass())};

			final Class<?>[] methodParameterClasses = functionalInterfaceMethod.getParameterTypes();
			methodParameterTypes = new Type[methodParameterClasses.length];
			for (int i = 0; i < methodParameterClasses.length; i++) {
				final Class<?> methodParameterType = methodParameterClasses[i];
				methodParameterTypes[i] = Type.getType(methodParameterType);
			}
		} else {
			wrappedFromSignature = context.getMethodSignature(toType.header, true);
			methodDescriptor = context.getMethodDescriptor(toType.header);
			methodSignature = context.getMethodSignature(toType.header);
			interfaces = new String[]{context.getInternalName(toType)};

			JavaSynthesizedFunctionInstance function = context.getFunction(toType);

			implementationMethod = new JavaNativeMethod(
					classInfo,
					JavaNativeMethod.Kind.COMPILED,
					function.getMethod(),
					true,
					methodDescriptor,
					JavaModifiers.PUBLIC,
					false // TODO: generic result or not
			);

			methodParameterTypes = new Type[toType.header.parameters.length];
			for (int i = 0; i < methodParameterTypes.length; i++) {
				methodParameterTypes[i] = context.getType(toType.header.parameters[i].type);
			}
		}

		final JavaNativeMethod wrappedMethod = context.getFunctionalInterface(fromType);
		final String constructorDescriptor = "(" + wrappedFromSignature + ")V";
		// ToDo: Is signature===descriptor fine here or do we need the actual signature here?
		final String constructorSignature = "(" + wrappedFromSignature + ")V";

		final ClassWriter lambdaCW = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
		lambdaCW.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", interfaces);

		//The field storing the wrapped object
		{
			lambdaCW.visitField(Modifier.PRIVATE | Modifier.FINAL, "wrapped", wrappedFromSignature, null, null).visitEnd();
		}

		//Constructor
		{
			JavaNativeMethod constructor = JavaNativeMethod.getConstructor(classInfo, constructorDescriptor, Opcodes.ACC_PUBLIC);
			JavaCompilingMethod compiling = new JavaCompilingMethod(classInfo, constructor, constructorSignature);
			final JavaWriter constructorWriter = new JavaWriter(context.logger, position, lambdaCW, compiling, null);
			constructorWriter.start();
			constructorWriter.loadObject(0);
			constructorWriter.dup();
			constructorWriter.invokeSpecial(Object.class, "<init>", "()V");

			constructorWriter.loadObject(1);
			constructorWriter.putField(className, "wrapped", wrappedFromSignature);

			constructorWriter.ret();
			constructorWriter.end();
		}

		//The actual method
		{
			JavaCompilingMethod compiling = new JavaCompilingMethod(classInfo, implementationMethod, methodSignature);
			final JavaWriter functionWriter = new JavaWriter(context.logger, position, lambdaCW, compiling, null);
			functionWriter.start();

			//this.wrapped
			functionWriter.loadObject(0);
			functionWriter.getField(className, "wrapped", wrappedFromSignature);

			//Load all function parameters
			for (int i = 0; i < methodParameterTypes.length; i++) {
				functionWriter.load(methodParameterTypes[i], i + 1);
			}

			//Invokes the wrapped interface's method and returns the result
			functionWriter.invokeInterface(wrappedMethod);
			final TypeID returnType = fromType.header.getReturnType();
			final Type rtype = context.getType(returnType);
			if (!CompilerUtils.isPrimitive(returnType)) {
				functionWriter.checkCast(rtype);
			}
			functionWriter.returnType(rtype);
			functionWriter.end();
		}

		lambdaCW.visitEnd();
		context.register(className, lambdaCW.toByteArray());

		return new FunctionCastWrapperClass(className, constructorDescriptor);
	}

	@Override
	public Void visitSupertypeCast(SupertypeCastExpression expression) {
		expression.value.accept(this);
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
		javaWriter.load(context.getType(expression.type), 0);
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
		expression.value.type.accept(expression.value.type, boxingTypeVisitor);
		return null;
	}

	public JavaWriter getJavaWriter() {
		return javaWriter;
	}

	private static class FunctionCastWrapperClass {
		final String className;
		final String constructorDesc;

		FunctionCastWrapperClass(String className, String constructorDesc) {
			this.className = className;
			this.constructorDesc = constructorDesc;
		}
	}
}
