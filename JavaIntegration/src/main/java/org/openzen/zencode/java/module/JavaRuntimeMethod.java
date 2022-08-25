package org.openzen.zencode.java.module;

import org.objectweb.asm.Type;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

public class JavaRuntimeMethod implements JavaMethod, MethodSymbol {
	private final JavaRuntimeClass class_;
	private final TypeSymbol target;
	private final JavaNativeMethod method;
	private final Modifiers modifiers;
	private final OperatorType operator;
	private final FunctionHeader header;

	public JavaRuntimeMethod(JavaRuntimeClass class_, TypeSymbol target, Constructor<?> constructor) {
		method = new JavaNativeMethod(
				class_.javaClass,
				JavaNativeMethod.Kind.CONSTRUCTOR,
				"init",
				false,
				Type.getConstructorDescriptor(constructor),
				constructor.getModifiers(),
				false);
		this.class_ = class_;
		this.target = target;
		modifiers = getMethodModifiers(constructor);
		operator = OperatorType.CONSTRUCTOR;
	}

	public JavaRuntimeMethod(JavaRuntimeClass class_, TypeSymbol target, Method method, TypeID result) {
		JavaNativeMethod.Kind kind;
		if (method.getName().equals("<init>"))
			kind = JavaNativeMethod.Kind.CONSTRUCTOR;
		else if (method.getName().equals("<clinit>"))
			kind = JavaNativeMethod.Kind.STATICINIT;
		else if (Modifier.isStatic(method.getModifiers()))
			kind = JavaNativeMethod.Kind.STATIC;
		else if (method.getDeclaringClass().isInterface())
			kind = JavaNativeMethod.Kind.INTERFACE;
		else
			kind = JavaNativeMethod.Kind.INSTANCE;

		final int length = method.getTypeParameters().length;
		boolean compile = length > 0 && length == Arrays.stream(method.getParameterTypes())
				.filter(s -> s.getCanonicalName().contentEquals("java.lang.Class"))
				.count();

		this.class_ = class_;
		this.target = target;
		this.method = new JavaNativeMethod(class_.javaClass, kind, method.getName(), compile, org.objectweb.asm.Type.getMethodDescriptor(method), method
				.getModifiers(), result.isGeneric());
		modifiers = getMethodModifiers(method);

		ZenCodeType.Operator operator = method.getAnnotation(ZenCodeType.Operator.class);
		this.operator = operator == null ? null : OperatorType.valueOf(operator.value().toString());
	}

	/* MethodSymbol implementation */

	@Override
	public DefinitionSymbol getDefiningType() {
		return class_;
	}

	@Override
	public TypeSymbol getTargetType() {
		return target;
	}

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	@Override
	public String getName() {
		return method.name;
	}

	@Override
	public Optional<OperatorType> getOperator() {
		return Optional.ofNullable(operator);
	}

	@Override
	public FunctionHeader getHeader() {
		return header;
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return Optional.empty(); // not always correct, but may not really matter in this context...
	}

	/* JavaMethod implementation */

	@Override
	public <T> T compileConstructor(JavaMethodCompiler<T> compiler, TypeID type, CallArguments arguments) {
		return null;
	}

	@Override
	public <T> T compileVirtual(JavaMethodCompiler<T> compiler, TypeID returnType, Expression target, CallArguments arguments) {
		return null;
	}

	@Override
	public <T> T compileStatic(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments) {
		return null;
	}


	private Modifiers getMethodModifiers(Member method) {
		Modifiers result = Modifiers.PUBLIC;
		if (Modifier.isStatic(method.getModifiers()))
			result = result.withStatic();
		if (Modifier.isFinal(method.getModifiers()))
			result = result.withFinal();

		return result;
	}
}
