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
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;

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

	public JavaRuntimeMethod(JavaRuntimeClass class_, TypeID target, Constructor<?> constructor, FunctionHeader header, boolean implicit) {
		this.method = new JavaNativeMethod(
				class_.javaClass,
				JavaNativeMethod.Kind.CONSTRUCTOR,
				"<init>",
				false,
				Type.getConstructorDescriptor(constructor),
				constructor.getModifiers(),
				false);
		this.class_ = class_;
		this.target = target;
		this.header = header;
		this.modifiers = getMethodModifiers(constructor, implicit, false).withStatic(); // In ZC, .ctors are static
		this.id = MethodID.staticOperator(OperatorType.CONSTRUCTOR);
	}

	public JavaRuntimeMethod(JavaRuntimeClass class_, TypeID target, Method method, MethodID id, FunctionHeader header, boolean implicit) {
		this(class_, target, method, id, header, implicit, false);
	}

	public JavaRuntimeMethod(JavaRuntimeClass class_, TypeID target, Method method, MethodID id, FunctionHeader header, boolean implicit, boolean expansion) {
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
				.getModifiers(), header.getReturnType().isGeneric(), calculateTypeParameterArguments(method, expansion));
		modifiers = getMethodModifiers(method, implicit, expansion);
		this.id = id;
		this.header = header;
	}

	private static boolean[] calculateTypeParameterArguments(Method method, boolean expansion) {
		boolean[] typeArguments = new boolean[method.getTypeParameters().length];

		int i = 0;
		boolean expandedTypeFound = false;
		for (Class<?> parameterType : method.getParameterTypes()) {
			if (parameterType == Class.class) {
				typeArguments[i] = true;
				i++;
			} else if (expansion && !expandedTypeFound) {
				expandedTypeFound = true;
			} else {
				break;
			}
		}
		return typeArguments;
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
	public <T> T compileSpecial(JavaMethodCompiler<T> compiler, TypeID returnType, Expression target, CallArguments arguments) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMapping(JavaClass class_) {
		return method.getMapping(class_);
	}

	@Override
	public JavaCompilingMethod asCompilingMethod(JavaClass compiled, String signature) {
		return new JavaCompilingMethod(compiled, signature);
	}

	private Modifiers getMethodModifiers(Member method, boolean implicit, boolean expansion) {
		Modifiers result = Modifiers.PUBLIC;
		if (Modifier.isStatic(method.getModifiers()) && !expansion)
			result = result.withStatic();
		if (Modifier.isFinal(method.getModifiers()))
			result = result.withFinal();
		if (implicit)
			result = result.withImplicit();

		return result;
	}
}
