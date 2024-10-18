/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;

import java.util.Arrays;

/**
 * @author Hoofdgebruiker
 */
public class JavaNativeMethod implements JavaMethod {
	public final JavaClass cls;
	public final Kind kind;
	public final String name;
	public final boolean compile;
	public final String descriptor;
	public final int modifiers; // these are Java modifiers!
	public final boolean genericResult;
	public final boolean[] typeParameterArguments;
	public final boolean[] primitiveArguments;

	public JavaNativeMethod(JavaClass cls, Kind kind, String name, boolean compile, String descriptor, int modifiers, boolean genericResult) {
		this(cls, kind, name, compile, descriptor, modifiers, genericResult, new boolean[0]);
	}

	public JavaNativeMethod(JavaClass cls, Kind kind, String name, boolean compile, String descriptor, int modifiers, boolean genericResult, boolean[] typeParameterArguments) {
		if (descriptor.contains("<")) // fix signature bug
			throw new IllegalArgumentException("Invalid descriptor!");
		if (cls.isInterface() && !JavaModifiers.isStatic(modifiers))
			kind = Kind.INTERFACE;

		this.cls = cls;
		this.kind = kind;
		this.name = name;
		this.compile = compile;

		this.descriptor = descriptor;
		this.modifiers = modifiers;
		this.genericResult = genericResult;
		this.typeParameterArguments = typeParameterArguments;
		this.primitiveArguments = determinePrimitiveArguments(descriptor);
	}

	public static JavaNativeMethod getConstructor(JavaClass cls, String descriptor, int modifiers) {
		return new JavaNativeMethod(cls, Kind.CONSTRUCTOR, "<init>", true, descriptor, modifiers, false);
	}

	public static JavaNativeMethod getNativeConstructor(JavaClass cls, String descriptor) {
		return new JavaNativeMethod(cls, Kind.CONSTRUCTOR, "<init>", false, descriptor, JavaModifiers.PUBLIC, false);
	}

	public static JavaNativeMethod getDestructor(JavaClass cls, int modifiers) {
		return new JavaNativeMethod(cls, Kind.INSTANCE, "close", true, "()V", modifiers, false);
	}

	public static JavaNativeMethod getStatic(JavaClass cls, String name, String descriptor, int modifiers) {
		return getStatic(cls, name, descriptor, modifiers, false);
	}

	public static JavaNativeMethod getStatic(JavaClass cls, String name, String descriptor, int modifiers, boolean genericResult) {
		return new JavaNativeMethod(cls, Kind.STATIC, name, true, descriptor, modifiers | JavaModifiers.STATIC, genericResult);
	}

	public static JavaNativeMethod getNativeStatic(JavaClass cls, String name, String descriptor) {
		return new JavaNativeMethod(cls, Kind.STATIC, name, false, descriptor, JavaModifiers.STATIC | JavaModifiers.PUBLIC, false);
	}

	public static JavaNativeMethod getVirtual(JavaClass cls, String name, String descriptor, int modifiers) {
		return getVirtual(cls, name, descriptor, modifiers, false);
	}

	public static JavaNativeMethod getVirtual(JavaClass cls, String name, String descriptor, int modifiers, boolean genericResult) {
		return new JavaNativeMethod(cls, Kind.INSTANCE, name, true, descriptor, modifiers, genericResult);
	}

	public static JavaNativeMethod getNativeVirtual(JavaClass cls, String name, String descriptor) {
		return new JavaNativeMethod(cls, Kind.INSTANCE, name, false, descriptor, JavaModifiers.PUBLIC, false);
	}

	public static JavaNativeMethod getInterface(JavaClass cls, String name, String descriptor) {
		return getInterface(cls, name, descriptor, false);
	}

	public static JavaNativeMethod getInterface(JavaClass cls, String name, String descriptor, boolean genericResult) {
		return new JavaNativeMethod(cls, Kind.INTERFACE, name, false, descriptor, JavaModifiers.PUBLIC, genericResult);
	}

	public static JavaNativeMethod getNativeExpansion(JavaClass cls, String name, String descriptor) {
		return new JavaNativeMethod(cls, Kind.EXPANSION, name, false, descriptor, JavaModifiers.PUBLIC | JavaModifiers.STATIC, false);
	}

	public String getMapping(JavaClass definition) {
		if (cls == null)
			return "!TODO";

		StringBuilder result = new StringBuilder();
		result.append(name);
		result.append(descriptor);

		if (definition == null || !definition.internalName.equals(cls.internalName))
			result.append('@').append(cls.internalName);

		return result.toString();
	}

	@Override
	public JavaCompilingMethod asCompilingMethod(JavaClass compiled, String signature) {
		return new JavaCompilingMethod(compiled, this, signature, compile);
	}

	public boolean isAbstract() {
		return (modifiers & JavaModifiers.ABSTRACT) > 0;
	}

	public JavaNativeMethod createBridge(String descriptor) {
		return new JavaNativeMethod(cls, kind, name, compile, descriptor, modifiers, genericResult, typeParameterArguments);
	}

	@Override
	public <T> T compileConstructor(JavaMethodCompiler<T> compiler, TypeID type, CallArguments arguments) {
		if(this.kind == Kind.STATIC) {
			return compiler.nativeStaticMethod(this, type, arguments);
		}

		return compiler.nativeConstructor(this, type, arguments);
	}

	@Override
	public <T> T compileBaseConstructor(JavaMethodCompiler<T> compiler, TypeID type, CallArguments arguments) {
		return compiler.nativeBaseConstructor(this, type, arguments);
	}

	@Override
	public <T> T compileVirtual(JavaMethodCompiler<T> compiler, TypeID returnType, Expression target, CallArguments arguments) {
		return compiler.nativeVirtualMethod(this, returnType, target, arguments);
	}

	@Override
	public <T> T compileVirtualWithTargetOnTopOfStack(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments) {
		return compiler.nativeStaticMethod(this, returnType, arguments);
	}

	@Override
	public <T> T compileStatic(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments) {
		return compiler.nativeStaticMethod(this, returnType, arguments);
	}

	@Override
	public <T> T compileSpecial(JavaMethodCompiler<T> compiler, TypeID returnType, Expression target, CallArguments arguments) {
		return compiler.nativeSpecialMethod(this, returnType, target, arguments);
	}

	public enum Kind {
		STATIC,
		STATICINIT,
		INSTANCE,
		INTERFACE,
		EXPANSION,
		CONSTRUCTOR,
		COMPILED
	}

	private static boolean[] determinePrimitiveArguments(String descriptor) {
		try {
			boolean[] result = new boolean[descriptor.length()];
			int index = 0;
			for (int i = 1; i < descriptor.length(); i++) {
				if (descriptor.charAt(i) == ')')
					break;

				char c = descriptor.charAt(i);
				if (c == 'L') {
					while (descriptor.charAt(i) != ';')
						i++;
					result[index++] = false;
				} else if (c == '[') {
					while (descriptor.charAt(i) == '[')
						i++;
					if (descriptor.charAt(i) == 'L') {
						while (descriptor.charAt(i) != ';')
							i++;
					}
					result[index++] = false;
				} else {
					result[index++] = true;
				}
			}
			return Arrays.copyOf(result, index);
		} catch (StringIndexOutOfBoundsException ex) {
			throw new IllegalArgumentException("Invalid descriptor: " + descriptor, ex);
		}
	}
}
