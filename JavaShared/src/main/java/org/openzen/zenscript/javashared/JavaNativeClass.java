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

	public void addConstructor(String key, String descriptor) {
		methods.put(key, createMethod("<init>", descriptor, JavaMethod.Kind.CONSTRUCTOR));
	}

	public void addInstanceMethod(String key, String name, String descriptor) {
		addInstanceMethod(key, name, descriptor, false);
	}

	public void addInstanceMethod(String key, String name, String descriptor, boolean genericReturnType) {
		methods.put(key, createMethod(name, descriptor, JavaMethod.Kind.INSTANCE, genericReturnType));
	}

	public JavaMethod createMethod(String name, String descriptor, JavaMethod.Kind instance) {
		return createMethod(name, descriptor, instance, false);
	}

	public JavaMethod createMethod(String name, String descriptor, JavaMethod.Kind instance, boolean genericReturnType) {
		return new JavaMethod(cls, instance, name, false, descriptor, JavaModifiers.PUBLIC, genericReturnType);
	}

	public JavaMethod createInstanceMethod(String name, String descriptor) {
		return createMethod(name, descriptor, JavaMethod.Kind.INSTANCE);
	}

	public JavaMethod getMethod(String name) {
		return methods.get(name);
	}
}
