/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.Opcodes;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaMethodInfo {
	public final JavaClassInfo javaClass;
	public final String name;
	public final String descriptor;
	public final int modifiers;
	
	public JavaMethodInfo(JavaClassInfo javaClass, String name, String signature, int modifiers) {
		this.javaClass = javaClass;
		this.name = name;
		this.descriptor = signature;
		this.modifiers = modifiers;
	}
	
	public boolean isStatic() {
		return (modifiers & Opcodes.ACC_STATIC) > 0;
	}
}
