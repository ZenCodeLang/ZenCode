/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import stdlib.Strings;

import java.io.Closeable;
import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Hoofdgebruiker
 */
public class JavaClass implements Comparable<JavaClass> {
	public static final JavaClass CLASS = fromJavaClass(Class.class);
	public static final JavaClass ENUM = fromJavaClass(Enum.class);
	public static final JavaClass OBJECT = fromJavaClass(Object.class);
	public static final JavaClass STRING = fromJavaClass(String.class);
	public static final JavaClass AUTO_CLOSEABLE = fromJavaClass(AutoCloseable.class);
	public static final JavaClass CLOSEABLE = fromJavaClass(Closeable.class);
	public static final JavaClass MAP = fromJavaClass(Map.class);
	public static final JavaClass HASHMAP = fromJavaClass(HashMap.class);
	public static final JavaClass ITERATOR = fromJavaClass(Iterator.class);
	public static final JavaClass ITERABLE = fromJavaClass(Iterable.class);
	public static final JavaClass ARRAYS = fromJavaClass(Arrays.class);

	public static final JavaClass BOOLEAN = fromJavaClass(Boolean.class);
	public static final JavaClass BYTE = fromJavaClass(Byte.class);
	public static final JavaClass SHORT = fromJavaClass(Short.class);
	public static final JavaClass INTEGER = fromJavaClass(Integer.class);
	public static final JavaClass LONG = fromJavaClass(Long.class);
	public static final JavaClass FLOAT = fromJavaClass(Float.class);
	public static final JavaClass DOUBLE = fromJavaClass(Double.class);
	public static final JavaClass CHARACTER = fromJavaClass(Character.class);
	public static final JavaClass COLLECTION = fromJavaClass(Collection.class);
	public static final JavaClass COLLECTIONS = fromJavaClass(Collections.class);
	public static final JavaClass STRING_BUILDER = fromJavaClass(StringBuilder.class);
	public static final JavaClass ARRAY = fromJavaClass(Array.class);

	public static final JavaClass SHARED = new JavaClass("zsynthetic", "Shared", Kind.CLASS);
	public final JavaClass outer;
	public final String pkg;
	public final String fullName;
	public final String internalName;
	public final Kind kind;
	private final String[] classNameParts;
	public boolean empty = false;
	public boolean membersPrepared = false;

	private JavaClass(String pkg, String internalName, Kind kind, String[] classNameParts) {
		if (classNameParts.length > 1) {
			String[] outerParts = Arrays.copyOf(classNameParts, classNameParts.length - 1);
			outer = new JavaClass(pkg, internalName.substring(0, internalName.lastIndexOf('$')), kind, outerParts);
		} else {
			outer = null;
		}

		this.pkg = pkg;
		this.fullName = String.join(".", classNameParts);
		this.internalName = internalName;
		this.kind = kind;
		this.classNameParts = classNameParts;
	}

	public JavaClass(String pkg, String name, Kind kind) {
		this.pkg = pkg;
		this.classNameParts = new String[]{name};
		this.fullName = pkg + '.' + name;
		this.internalName = pkg.isEmpty() ? name : pkg.replace('.', '/') + '/' + name;
		this.kind = kind;

		outer = this;
	}

	public JavaClass(JavaClass outer, String name, Kind kind) {
		this.pkg = outer.pkg;
		this.classNameParts = Arrays.copyOf(outer.classNameParts, outer.classNameParts.length + 1);
		this.classNameParts[outer.classNameParts.length] = name;
		this.fullName = outer.fullName + '.' + name;
		this.internalName = outer.internalName + '$' + name;
		this.kind = kind;

		this.outer = outer.outer;
	}

	public static JavaClass fromInternalName(String internalName, Kind kind) {
		if (kind == Kind.ARRAY)
			return new JavaClass("", internalName, kind, new String[0]);

		int lastSlash = internalName.lastIndexOf('/');
		//if (lastSlash < 0)
		//	System.out.println(internalName);

		String pkg = lastSlash < 0 ? "" : internalName.substring(0, lastSlash);
		String className = lastSlash < 0 ? internalName : internalName.substring(lastSlash + 1);
		String[] nameParts = Strings.split(className, '$');
		return new JavaClass(pkg, internalName, kind, nameParts);
	}

	public static JavaClass fromJavaClass(final Class<?> clazz) {
		if (clazz.isArray()) {
			return JavaClass.fromInternalName(clazz.getName(), Kind.ARRAY);
		}
		if (clazz.isPrimitive()) {
			throw new IllegalStateException("JavaClass cannot represent primitive types");
		}

		final String internalName = clazz.getName().replace('.', '/');
		final Kind kind = clazz.isInterface()? Kind.INTERFACE : clazz.isEnum()? Kind.ENUM : Kind.CLASS;
		return JavaClass.fromInternalName(internalName, kind);
	}

	public static String getNameFromFile(String filename) {
		if (filename.indexOf('.') > 0)
			return filename.substring(0, filename.lastIndexOf('.'));
		else
			return filename;
	}

	public boolean isPrimitive() {
		return false;
	}

	/**
	 * Retrieves the name of the class itself, excluding outer class or package
	 * name.
	 *
	 * @return
	 */
	public String getName() {
		return classNameParts[classNameParts.length - 1];
	}

	/**
	 * Retrieves the full name of the class, including outer class but excluding
	 * package name.
	 *
	 * @return
	 */
	public String getClassName() {
		return fullName.substring(pkg.length() + 1);
	}

	@Override
	public int compareTo(JavaClass o) {
		return fullName.compareTo(o.fullName);
	}

	public boolean isEnum() {
		return kind == Kind.ENUM;
	}

	public boolean isInterface() {
		return kind == Kind.INTERFACE;
	}

	public enum Kind {
		CLASS,
		INTERFACE,
		ENUM,
		ARRAY,
		EXPANSION
	}
}
