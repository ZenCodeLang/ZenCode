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
	public final JavaClass cls;
	public final Kind kind;
	public final String name;
	public final boolean compile;
	public final JavaNativeTranslation translation;
	
	public final String descriptor;
	public final int modifiers;
	
	public JavaMethod(JavaClass cls, Kind kind, String name, boolean compile, String descriptor, int modifiers) {
		this.cls = cls;
		this.kind = kind;
		this.name = name;
		this.compile = compile;
		translation = null;
		
		this.descriptor = descriptor;
		this.modifiers = modifiers;
	}
	
	public JavaMethod(JavaNativeTranslation<?> translation) {
		this.cls = null;
		this.kind = Kind.COMPILED;
		this.name = null;
		this.compile = false;
		this.translation = translation;
		this.descriptor = "";
		this.modifiers = 0;
	}
	
	public enum Kind {
		STATIC,
		INSTANCE,
		EXPANSION,
		CONSTRUCTOR,
		COMPILED
	}
}
