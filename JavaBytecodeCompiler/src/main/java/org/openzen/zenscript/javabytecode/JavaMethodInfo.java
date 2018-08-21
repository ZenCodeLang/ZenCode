/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.javashared.JavaClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaMethodInfo {
	public static JavaMethodInfo get(int modifiers, Class owner, String name, Class result, Class... arguments) {
        StringBuilder descriptor = new StringBuilder();
        descriptor.append('(');
        for (Class argument : arguments) {
            descriptor.append(Type.getDescriptor(argument));
        }
        descriptor.append(')');
        descriptor.append(result == null ? 'V' : Type.getDescriptor(result));
		return new JavaMethodInfo(JavaCompileUtils.get(owner), name, descriptor.toString(), modifiers);
    }
	
	public final JavaClass javaClass;
	public final String name;
	public final String descriptor;
	public final int modifiers;
	
	public JavaMethodInfo(JavaClass javaClass, String name, String signature, int modifiers) {
		this.javaClass = javaClass;
		this.name = name;
		this.descriptor = signature;
		this.modifiers = modifiers;
	}
	
	public boolean isStatic() {
		return (modifiers & Opcodes.ACC_STATIC) > 0;
	}
}
