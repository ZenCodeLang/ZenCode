/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.tags;

import org.openzen.zenscript.javasource.JavaCallCompiler;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceMethod {
	public final JavaSourceClass cls;
	public final Kind kind;
	public final String name;
	public final boolean compile;
	public final JavaCallCompiler compiler;
	
	public JavaSourceMethod(JavaSourceClass cls, Kind kind, String name, boolean compile) {
		this.cls = cls;
		this.kind = kind;
		this.name = name;
		this.compile = compile;
		compiler = null;
	}
	
	public JavaSourceMethod(JavaCallCompiler compiler) {
		this.cls = null;
		this.kind = Kind.COMPILED;
		this.name = null;
		this.compile = false;
		this.compiler = compiler;
	}
	
	public enum Kind {
		STATIC,
		INSTANCE,
		EXPANSION,
		CONSTRUCTOR,
		COMPILED
	}
}
