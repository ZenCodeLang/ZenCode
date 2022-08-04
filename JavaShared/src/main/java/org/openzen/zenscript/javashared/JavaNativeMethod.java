/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 * @author Hoofdgebruiker
 */
public class JavaNativeMethod implements JavaMethod {
	public final JavaClass cls;
	public final Kind kind;
	public final String name;
	public final boolean compile;
	public final JavaNativeTranslation translation;
	public final String descriptor;
	public final int modifiers;
	public final boolean genericResult;
	public final boolean[] typeParameterArguments;

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
		translation = null;

		this.descriptor = descriptor;
		this.modifiers = modifiers;
		this.genericResult = genericResult;
		this.typeParameterArguments = typeParameterArguments;
	}

	public JavaNativeMethod(JavaNativeTranslation<?> translation) {
		this.cls = null;
		this.kind = Kind.COMPILED;
		this.name = null;
		this.compile = false;
		this.translation = translation;
		this.descriptor = "";
		this.modifiers = 0;
		this.genericResult = false;
		this.typeParameterArguments = new boolean[0];
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
		return new JavaNativeMethod(cls, Kind.STATIC, name, true, descriptor, modifiers | JavaModifiers.STATIC, false);
	}

	public static JavaNativeMethod getNativeStatic(JavaClass cls, String name, String descriptor) {
		return new JavaNativeMethod(cls, Kind.STATIC, name, false, descriptor, JavaModifiers.STATIC | JavaModifiers.PUBLIC, false);
	}

	public static JavaNativeMethod getVirtual(JavaClass cls, String name, String descriptor, int modifiers) {
		return new JavaNativeMethod(cls, Kind.INSTANCE, name, true, descriptor, modifiers, false);
	}

	public static JavaNativeMethod getNativeVirtual(JavaClass cls, String name, String descriptor) {
		return new JavaNativeMethod(cls, Kind.INSTANCE, name, false, descriptor, JavaModifiers.PUBLIC, false);
	}

	public static JavaNativeMethod getInterface(JavaClass cls, String name, String descriptor) {
		return new JavaNativeMethod(cls, Kind.INTERFACE, name, false, descriptor, JavaModifiers.PUBLIC, false);
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

	public boolean isAbstract() {
		return (modifiers & JavaModifiers.ABSTRACT) > 0;
	}

	@Override
	public <T> T compile(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments) {
		return compiler.nativeMethod(this, returnType, arguments);
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
}
