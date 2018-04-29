/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

/**
 *
 * @author Hoofdgebruiker
 */
public class Modifiers {
	private Modifiers() {}
	
	public static final int PUBLIC = 1;
	public static final int EXPORT = 2;
	public static final int PRIVATE = 4;
	public static final int ABSTRACT = 8;
	public static final int FINAL = 16;
	public static final int CONST = 32;
	public static final int CONST_OPTIONAL = 64;
	public static final int STATIC = 128;
	public static final int PROTECTED = 256;
	public static final int IMPLICIT = 512;
	
	public static boolean isAbstract(int modifiers) {
		return (modifiers & ABSTRACT) > 0;
	}
	
	public static boolean isFinal(int modifiers) {
		return (modifiers & FINAL) > 0;
	}
	
	public static boolean isConst(int modifiers) {
		return (modifiers & CONST) > 0;
	}
	
	public static boolean isStatic(int modifiers) {
		return (modifiers & STATIC) > 0;
	}
	
	public static boolean isImplicit(int modifiers) {
		return (modifiers & IMPLICIT) > 0;
	}
}
