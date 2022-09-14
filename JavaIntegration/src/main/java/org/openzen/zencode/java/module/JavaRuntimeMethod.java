package org.openzen.zencode.java.module;

import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
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
	private final TypeID target;
	private final JavaNativeMethod method;
	private final Modifiers modifiers;
	private final FunctionHeader header;
	private final MethodID id;

	public JavaRuntimeMethod(JavaRuntimeClass class_, TypeID target, Constructor<?> constructor, FunctionHeader header) {
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
		this.header = header;
		this.id = MethodID.operator(OperatorType.CONSTRUCTOR);
	}

	public JavaRuntimeMethod(JavaRuntimeClass class_, TypeID target, Method method, MethodID id, FunctionHeader header) {
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
				.getModifiers(), header.getReturnType().isGeneric());
		modifiers = getMethodModifiers(method);
		this.id = id;
		this.header = header;
	}

	public JavaNativeMethod getNative() {
		return method;
	}

	/* MethodSymbol implementation */

	@Override
	public DefinitionSymbol getDefiningType() {
		return class_;
	}

	@Override
	public TypeID getTargetType() {
		return target;
	}

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	@Override
	public MethodID getID() {
		return id;
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
		return compiler.nativeConstructor(method, type, arguments);
	}

	@Override
	public <T> T compileVirtual(JavaMethodCompiler<T> compiler, TypeID returnType, Expression target, CallArguments arguments) {
		return compiler.nativeVirtualMethod(method, returnType, target, arguments);
	}

	@Override
	public <T> T compileStatic(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments) {
		return compiler.nativeStaticMethod(method, returnType, arguments);
	}

	@Override
	public String getMapping(JavaClass class_) {
		return method.getMapping(class_);
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
