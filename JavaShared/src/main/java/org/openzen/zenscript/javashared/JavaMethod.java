/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaMethod {
	public static JavaMethod getConstructor(JavaClass cls, String descriptor, int modifiers) {
		return new JavaMethod(cls, Kind.CONSTRUCTOR, "<init>", true, descriptor, modifiers, false);
	}
	
	public static JavaMethod getNativeConstructor(JavaClass cls, String descriptor) {
		return new JavaMethod(cls, Kind.CONSTRUCTOR, "<init>", false, descriptor, JavaModifiers.PUBLIC, false);
	}
	
	public static JavaMethod getDestructor(JavaClass cls, int modifiers) {
		return new JavaMethod(cls, Kind.INSTANCE, "close", true, "()V", modifiers, false);
	}
	
	public static JavaMethod getStatic(JavaClass cls, String name, String descriptor, int modifiers) {
		return new JavaMethod(cls, Kind.STATIC, name, true, descriptor, modifiers | JavaModifiers.STATIC, false);
	}
	
	public static JavaMethod getNativeStatic(JavaClass cls, String name, String descriptor) {
		return new JavaMethod(cls, Kind.STATIC, name, false, descriptor, JavaModifiers.STATIC | JavaModifiers.PUBLIC, false);
	}
	
	public static JavaMethod getVirtual(JavaClass cls, String name, String descriptor, int modifiers) {
		return new JavaMethod(cls, Kind.INSTANCE, name, true, descriptor, modifiers, false);
	}
	
	public static JavaMethod getNativeVirtual(JavaClass cls, String name, String descriptor) {
		return new JavaMethod(cls, Kind.INSTANCE, name, false, descriptor, JavaModifiers.PUBLIC, false);
	}

	public static JavaMethod getNativeExpansion(JavaClass cls, String name, String descriptor) {
		return new JavaMethod(cls, Kind.EXPANSION, name, false, descriptor, JavaModifiers.PUBLIC | JavaModifiers.STATIC, false);
	}
	
	public final JavaClass cls;
	public final Kind kind;
	public final String name;
	public final boolean compile;
	public final JavaNativeTranslation translation;
	
	public final String descriptor;
	public final int modifiers;
	public final boolean genericResult;
	public final boolean[] typeParameterArguments;
	
	public JavaMethod(JavaClass cls, Kind kind, String name, boolean compile, String descriptor, int modifiers, boolean genericResult) {
		this(cls, kind, name, compile, descriptor, modifiers, genericResult, new boolean[0]);
	}
	
	public JavaMethod(JavaClass cls, Kind kind, String name, boolean compile, String descriptor, int modifiers, boolean genericResult, boolean[] typeParameterArguments) {
		if (descriptor.contains("<")) // fix signature bug
			throw new IllegalArgumentException("Invalid descriptor!");
		
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
	
	public JavaMethod(JavaNativeTranslation<?> translation) {
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
	
	public enum Kind {
		STATIC,
		STATICINIT,
		INSTANCE,
		EXPANSION,
		CONSTRUCTOR,
		COMPILED
	}
}
