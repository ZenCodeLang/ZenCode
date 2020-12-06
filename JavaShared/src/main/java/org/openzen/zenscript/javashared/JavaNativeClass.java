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
		methods.put(key, new JavaMethod(cls, JavaMethod.Kind.CONSTRUCTOR, "<init>", false, descriptor, JavaModifiers.PUBLIC, false));
	}

	public void addInstanceMethod(String key, String name, String descriptor) {
		methods.put(key, new JavaMethod(cls, JavaMethod.Kind.INSTANCE, name, false, descriptor, JavaModifiers.PUBLIC, false));
	}

	public JavaMethod getMethod(String name) {
		return methods.get(name);
	}
}
