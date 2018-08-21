/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.Type;
import org.openzen.zenscript.javashared.JavaClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaCompileUtils {
	private JavaCompileUtils() {}
	
	public static JavaClass get(Class<?> cls) {
		return JavaClass.fromInternalName(
				Type.getInternalName(cls),
				cls.isInterface() ? JavaClass.Kind.INTERFACE : (cls.isEnum() ? JavaClass.Kind.ENUM : JavaClass.Kind.CLASS));
	}
}
