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
	public static final int VIRTUAL = 1024;
	public static final int EXTERN = 2048;
	public static final int OVERRIDE = 4096;
	
	public static boolean isPublic(int modifiers) {
		return (modifiers & PUBLIC) > 0;
	}
	
	public static boolean isExport(int modifiers) {
		return (modifiers & EXPORT) > 0;
	}
	
	public static boolean isProtected(int modifiers) {
		return (modifiers & PROTECTED) > 0;
	}
	
	public static boolean isPrivate(int modifiers) {
		return (modifiers & PRIVATE) > 0;
	}
	
	public static boolean isAbstract(int modifiers) {
		return (modifiers & ABSTRACT) > 0;
	}
	
	public static boolean isFinal(int modifiers) {
		return (modifiers & FINAL) > 0;
	}
	
	public static boolean isConst(int modifiers) {
		return (modifiers & CONST) > 0;
	}
	
	public static boolean isConstOptional(int modifiers) {
		return (modifiers & CONST_OPTIONAL) > 0;
	}
	
	public static boolean isStatic(int modifiers) {
		return (modifiers & STATIC) > 0;
	}
	
	public static boolean isImplicit(int modifiers) {
		return (modifiers & IMPLICIT) > 0;
	}
	
	public static boolean isVirtual(int modifiers) {
		return (modifiers & VIRTUAL) > 0;
	}
	
	public static boolean isExtern(int modifiers) {
		return (modifiers & EXTERN) > 0;
	}
}
