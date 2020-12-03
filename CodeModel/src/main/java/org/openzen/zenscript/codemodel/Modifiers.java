package org.openzen.zenscript.codemodel;

public class Modifiers {
	private Modifiers() {}
	
	public static final int PUBLIC = 1;
	public static final int INTERNAL = 2;
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
	
	public static boolean isInternal(int modifiers) {
		return (modifiers & INTERNAL) > 0;
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
	
	public static boolean isOverride(int modifiers) {
		return (modifiers & OVERRIDE) > 0;
	}
	
	public static boolean hasAccess(int modifiers) {
		return (modifiers & (PRIVATE | PUBLIC | PROTECTED | INTERNAL)) > 0;
	}
    
    public static String describe(int modifiers) {
        StringBuilder builder = new StringBuilder();
        if (isPublic(modifiers)) {
            builder.append("public");
        } else if (isPrivate(modifiers)) {
            builder.append("private");
        } else if (isProtected(modifiers)) {
            builder.append("protected");
        }
        if (isAbstract(modifiers)) {
            builder.append(" abstract");
        } else if (isFinal(modifiers)) {
            builder.append(" final");
        } else if (isStatic(modifiers)) {
            builder.append(" static");
        }
        return builder.toString();
    }
    
}
