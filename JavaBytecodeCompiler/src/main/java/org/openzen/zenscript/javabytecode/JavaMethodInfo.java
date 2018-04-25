/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaMethodInfo {
	public final JavaClassInfo javaClass;
	public final String name;
	public final String signature;
	public final boolean isStatic;
	
	public JavaMethodInfo(JavaClassInfo javaClass, String name, String signature) {
		this(javaClass, name, signature, false);
	}
	
	public JavaMethodInfo(JavaClassInfo javaClass, String name, String signature, boolean isStatic) {
		this.javaClass = javaClass;
		this.name = name;
		this.signature = signature;
		this.isStatic = isStatic;
	}
}
