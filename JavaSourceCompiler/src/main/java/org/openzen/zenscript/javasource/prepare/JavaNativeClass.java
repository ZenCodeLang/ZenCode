/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.prepare;

import java.util.HashMap;
import java.util.Map;
import org.openzen.zenscript.javasource.tags.JavaSourceClass;
import org.openzen.zenscript.javasource.tags.JavaSourceMethod;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaNativeClass {
	public final JavaSourceClass cls;
	private final Map<String, JavaSourceMethod> methods = new HashMap<>();
	
	public JavaNativeClass(JavaSourceClass cls) {
		this.cls = cls;
	}
	
	public void addMethod(String key, JavaSourceMethod method) {
		methods.put(key, method);
	}
	
	public void addConstructor(String key, String name) {
		methods.put(key, new JavaSourceMethod(cls, JavaSourceMethod.Kind.CONSTRUCTOR, name, false));
	}
	
	public void addInstanceMethod(String key, String name) {
		methods.put(key, new JavaSourceMethod(cls, JavaSourceMethod.Kind.INSTANCE, name, false));
	}
	
	public JavaSourceMethod getMethod(String name) {
		return methods.get(name);
	}
}
