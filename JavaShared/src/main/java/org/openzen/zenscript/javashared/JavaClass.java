/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import java.util.Arrays;
import stdlib.Strings;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaClass implements Comparable<JavaClass> {
	public static final JavaClass CLASS = new JavaClass("java.lang", "Class", Kind.CLASS);
	public static final JavaClass ENUM = new JavaClass("java.lang", "Enum", Kind.CLASS);
	public static final JavaClass OBJECT = new JavaClass("java.lang", "Object", Kind.CLASS);
	public static final JavaClass STRING = new JavaClass("java.lang", "String", Kind.CLASS);
	public static final JavaClass CLOSEABLE = new JavaClass("java.lang", "AutoCloseable", Kind.INTERFACE);
	public static final JavaClass MAP = new JavaClass("java.util", "Map", JavaClass.Kind.INTERFACE);
	public static final JavaClass HASHMAP = new JavaClass("java.util", "HashMap", JavaClass.Kind.CLASS);
	public static final JavaClass ITERATOR = new JavaClass("java.util", "Iterator", JavaClass.Kind.INTERFACE);
	public static final JavaClass ARRAYS = new JavaClass("java.util", "Arrays", Kind.CLASS);
	
	public static final JavaClass BOOLEAN = new JavaClass("java.lang", "Boolean", Kind.CLASS);
	public static final JavaClass BYTE = new JavaClass("java.lang", "Byte", Kind.CLASS);
	public static final JavaClass SHORT = new JavaClass("java.lang", "Short", Kind.CLASS);
	public static final JavaClass INTEGER = new JavaClass("java.lang", "Integer", Kind.CLASS);
	public static final JavaClass LONG = new JavaClass("java.lang", "Long", Kind.CLASS);
	public static final JavaClass FLOAT = new JavaClass("java.lang", "Float", Kind.CLASS);
	public static final JavaClass DOUBLE = new JavaClass("java.lang", "Double", Kind.CLASS);
	public static final JavaClass CHARACTER = new JavaClass("java.lang", "Character", Kind.CLASS);
	public static final JavaClass COLLECTION = new JavaClass("java.util", "Collection", Kind.INTERFACE);
	
	public static JavaClass fromInternalName(String internalName, Kind kind) {
		if (kind == Kind.ARRAY)
			return new JavaClass("", internalName, kind, new String[0]);
		
		int lastSlash = internalName.lastIndexOf('/');
		if (lastSlash < 0)
			System.out.println(internalName);
		
		String pkg = lastSlash < 0 ? "" : internalName.substring(0, lastSlash);
		String className = lastSlash < 0 ? internalName : internalName.substring(lastSlash + 1);
		String[] nameParts = Strings.split(className, '$');
		return new JavaClass(pkg, internalName, kind, nameParts);
	}
	
	public static String getNameFromFile(String filename) {
		if (filename.indexOf('.') > 0)
			return filename.substring(0, filename.indexOf('.'));
		else
			return filename;
	}
	
	public final JavaClass outer;
	
	public final String pkg;
	public final String fullName;
	public final String internalName;
	public final Kind kind;
	
	public boolean empty = false;
	public boolean destructible = false;
	public boolean membersPrepared = false;
	
	private final String[] classNameParts;
	
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
		this.classNameParts = new String[] { name };
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
	
	public enum Kind {
		CLASS,
		INTERFACE,
		ENUM,
		ARRAY
	}
}
