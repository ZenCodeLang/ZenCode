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
	public static final int MODIFIER_PUBLIC = 1;
	public static final int MODIFIER_EXPORT = 2;
	public static final int MODIFIER_PRIVATE = 4;
	public static final int MODIFIER_ABSTRACT = 8;
	public static final int MODIFIER_FINAL = 16;
	public static final int MODIFIER_CONST = 32;
	public static final int MODIFIER_CONST_OPTIONAL = 64;
	public static final int MODIFIER_STATIC = 128;
	public static final int MODIFIER_PROTECTED = 256;
	public static final int MODIFIER_IMPLICIT = 512;
	
	public static boolean isAbstract(int modifiers) {
		return (modifiers & MODIFIER_ABSTRACT) > 0;
	}
	
	public static boolean isFinal(int modifiers) {
		return (modifiers & MODIFIER_FINAL) > 0;
	}
	
	public static boolean isConst(int modifiers) {
		return (modifiers & MODIFIER_CONST) > 0;
	}
	
	public static boolean isStatic(int modifiers) {
		return (modifiers & MODIFIER_STATIC) > 0;
	}
	
	public static boolean isImplicit(int modifiers) {
		return (modifiers & MODIFIER_IMPLICIT) > 0;
	}
}
