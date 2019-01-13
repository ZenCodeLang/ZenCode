/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.Modifiers;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaModifiers {
	public static final int PUBLIC = 0x0001; // class, field, method
    public static final int PRIVATE = 0x0002; // class, field, method
    public static final int PROTECTED = 0x0004; // class, field, method
    public static final int STATIC = 0x0008; // field, method
    public static final int FINAL = 0x0010; // class, field, method, parameter
    public static final int SUPER = 0x0020; // class
    public static final int SYNCHRONIZED = 0x0020; // method
    public static final int OPEN = 0x0020; // module
    public static final int TRANSITIVE = 0x0020; // module requires
    public static final int VOLATILE = 0x0040; // field
    public static final int BRIDGE = 0x0040; // method
    public static final int STATIC_PHASE = 0x0040; // module requires
    public static final int VARARGS = 0x0080; // method
    public static final int TRANSIENT = 0x0080; // field
    public static final int NATIVE = 0x0100; // method
    public static final int INTERFACE = 0x0200; // class
    public static final int ABSTRACT = 0x0400; // class, method
    public static final int STRICT = 0x0800; // method
    public static final int SYNTHETIC = 0x1000; // class, field, method, parameter, module *
    public static final int ANNOTATION = 0x2000; // class
    public static final int ENUM = 0x4000; // class(?) field inner
    public static final int MANDATED = 0x8000; // parameter, module, module *
    public static final int MODULE = 0x8000; // class
	
	public static int getJavaModifiers(int modifiers) {
		int out = 0;
		if (Modifiers.isStatic(modifiers))
			out |= STATIC;
		if (Modifiers.isFinal(modifiers))
			out |= FINAL;
		if (Modifiers.isPublic(modifiers))
			out |= PUBLIC;
		if (Modifiers.isPrivate(modifiers))
			out |= PRIVATE;
		if (Modifiers.isProtected(modifiers))
			out |= PROTECTED;
		if (Modifiers.isAbstract(modifiers))
			out |= ABSTRACT;
		return out;
	}
	
	public static boolean isStatic(int modifiers) {
		return (modifiers & STATIC) > 0;
	}
	
	private JavaModifiers() {}
}
