/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hoofdgebruiker
 */
public class JavaNativeClass {
	public final JavaClass cls;
	public final boolean nonDestructible;
	private final Map<String, JavaMethod> methods = new HashMap<>();

	public JavaNativeClass(JavaClass cls) {
		this(cls, false);
	}

	public JavaNativeClass(JavaClass cls, boolean nonDestructible) {
		this.cls = cls;
		this.nonDestructible = nonDestructible;
	}

	public void addMethod(String key, JavaMethod method) {
		methods.put(key, method);
	}

	public void addConstructor(String key, String descriptor, String signature) {
		methods.put(key, createMethod("<init>", descriptor, signature, JavaNativeMethod.Kind.CONSTRUCTOR));
	}

	public void addInstanceMethod(String key, String name, String descriptor, String signature) {
		addInstanceMethod(key, name, descriptor, signature, false);
	}

	public void addInstanceMethod(String key, String name, String descriptor, String signature, boolean genericReturnType) {
		methods.put(key, createMethod(name, descriptor, signature, JavaNativeMethod.Kind.INSTANCE, genericReturnType));
	}

	public JavaNativeMethod createMethod(String name, String descriptor, String signature, JavaNativeMethod.Kind instance) {
		return createMethod(name, descriptor, signature, instance, false);
	}

	public JavaNativeMethod createMethod(String name, String descriptor, String signature, JavaNativeMethod.Kind instance, boolean genericReturnType) {
		return new JavaNativeMethod(cls, instance, name, false, descriptor, signature, JavaModifiers.PUBLIC, genericReturnType);
	}

	public JavaNativeMethod createInstanceMethod(String name, String descriptor, String signature) {
		return createMethod(name, descriptor, signature, JavaNativeMethod.Kind.INSTANCE);
	}

	public JavaMethod getMethod(String name) {
		return methods.get(name);
	}
}
