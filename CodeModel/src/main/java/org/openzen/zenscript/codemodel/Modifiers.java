package org.openzen.zenscript.codemodel;

public class Modifiers {
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

	public final int value;

	public Modifiers(int value) {
		this.value = value;
	}

	public boolean isPublic() {
		return (value & PUBLIC) > 0;
	}

	public Modifiers withPublic() {
		return new Modifiers(value | PUBLIC);
	}

	public static boolean isPublic(int modifiers) {
		return (modifiers & PUBLIC) > 0;
	}

	public boolean isInternal() {
		return (value & INTERNAL) > 0;
	}

	public Modifiers withInternal() {
		return new Modifiers(value | INTERNAL);
	}

	public static boolean isInternal(int modifiers) {
		return (modifiers & INTERNAL) > 0;
	}

	public boolean isProtected() {
		return (value & PROTECTED) > 0;
	}

	public Modifiers withProtected() {
		return new Modifiers(value | PROTECTED);
	}

	public static boolean isProtected(int modifiers) {
		return (modifiers & PROTECTED) > 0;
	}

	public boolean isPrivate() {
		return (value & PRIVATE) > 0;
	}

	public Modifiers withPrivate() {
		return new Modifiers(value | PRIVATE);
	}

	public static boolean isPrivate(int modifiers) {
		return (modifiers & PRIVATE) > 0;
	}

	public boolean isAbstract() {
		return (value & ABSTRACT) > 0;
	}

	public Modifiers withAbstract() {
		return new Modifiers(value | ABSTRACT);
	}


	public static boolean isAbstract(int modifiers) {
		return (modifiers & ABSTRACT) > 0;
	}

	public boolean isFinal() {
		return (value & FINAL) > 0;
	}

	public Modifiers withFinal() {
		return new Modifiers(value | FINAL);
	}

	public static boolean isFinal(int modifiers) {
		return (modifiers & FINAL) > 0;
	}

	public boolean isConst() {
		return (value & CONST) > 0;
	}

	public Modifiers withConst() {
		return new Modifiers(value | CONST);
	}

	public static boolean isConst(int modifiers) {
		return (modifiers & CONST) > 0;
	}

	public boolean isConstOptional() {
		return (value & CONST_OPTIONAL) > 0;
	}

	public Modifiers withConstOptional() {
		return new Modifiers(value | CONST_OPTIONAL);
	}

	public static boolean isConstOptional(int modifiers) {
		return (modifiers & CONST_OPTIONAL) > 0;
	}

	public boolean isStatic() {
		return (value & STATIC) > 0;
	}

	public Modifiers withStatic() {
		return new Modifiers(value | STATIC);
	}

	public static boolean isStatic(int modifiers) {
		return (modifiers & STATIC) > 0;
	}


	public boolean isImplicit() {
		return (value & IMPLICIT) > 0;
	}

	public Modifiers withImplicit() {
		return new Modifiers(value | IMPLICIT);
	}

	public static boolean isImplicit(int modifiers) {
		return (modifiers & IMPLICIT) > 0;
	}

	public boolean isVirtual() {
		return (value & VIRTUAL) > 0;
	}

	public Modifiers withVirtual() {
		return new Modifiers(value | VIRTUAL);
	}

	public static boolean isVirtual(int modifiers) {
		return (modifiers & VIRTUAL) > 0;
	}

	public boolean isExtern() {
		return (value & EXTERN) > 0;
	}

	public Modifiers withExtern() {
		return new Modifiers(value | EXTERN);
	}

	public static boolean isExtern(int modifiers) {
		return (modifiers & EXTERN) > 0;
	}

	public static boolean isOverride(int modifiers) {
		return (modifiers & OVERRIDE) > 0;
	}

	public boolean hasAccessModifiers() {
		return (value & (PRIVATE | PUBLIC | PROTECTED | INTERNAL)) > 0;
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
