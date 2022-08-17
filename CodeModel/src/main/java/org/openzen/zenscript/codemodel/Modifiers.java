package org.openzen.zenscript.codemodel;

public class Modifiers {
	public static final int FLAG_PUBLIC = 1;
	public static final int FLAG_INTERNAL = 2;
	public static final int FLAG_PRIVATE = 4;
	public static final int FLAG_ABSTRACT = 8;
	public static final int FLAG_FINAL = 16;
	public static final int FLAG_CONST = 32;
	public static final int FLAG_CONST_OPTIONAL = 64;
	public static final int FLAG_STATIC = 128;
	public static final int FLAG_PROTECTED = 256;
	public static final int FLAG_IMPLICIT = 512;
	public static final int FLAG_VIRTUAL = 1024;
	public static final int FLAG_EXTERN = 2048;
	public static final int FLAG_OVERRIDE = 4096;

	public static final Modifiers NONE = new Modifiers(0);
	public static final Modifiers PUBLIC = new Modifiers(FLAG_PUBLIC);
	public static final Modifiers PUBLIC_STATIC = new Modifiers(FLAG_PUBLIC | FLAG_STATIC);
	public static final Modifiers PRIVATE = new Modifiers(FLAG_PRIVATE);

	public final int value;

	public Modifiers(int value) {
		this.value = value;
	}

	public boolean isPublic() {
		return (value & FLAG_PUBLIC) > 0;
	}

	public Modifiers withPublic() {
		return new Modifiers(value | FLAG_PUBLIC);
	}

	public static boolean isPublic(int modifiers) {
		return (modifiers & FLAG_PUBLIC) > 0;
	}

	public boolean isInternal() {
		return (value & FLAG_INTERNAL) > 0;
	}

	public Modifiers withInternal() {
		return new Modifiers(value | FLAG_INTERNAL);
	}

	public static boolean isInternal(int modifiers) {
		return (modifiers & FLAG_INTERNAL) > 0;
	}

	public boolean isProtected() {
		return (value & FLAG_PROTECTED) > 0;
	}

	public Modifiers withProtected() {
		return new Modifiers(value | FLAG_PROTECTED);
	}

	public static boolean isProtected(int modifiers) {
		return (modifiers & FLAG_PROTECTED) > 0;
	}

	public boolean isPrivate() {
		return (value & FLAG_PRIVATE) > 0;
	}

	public Modifiers withPrivate() {
		return new Modifiers(value | FLAG_PRIVATE);
	}

	public static boolean isPrivate(int modifiers) {
		return (modifiers & FLAG_PRIVATE) > 0;
	}

	public boolean isAbstract() {
		return (value & FLAG_ABSTRACT) > 0;
	}

	public Modifiers withAbstract() {
		return new Modifiers(value | FLAG_ABSTRACT);
	}


	public static boolean isAbstract(int modifiers) {
		return (modifiers & FLAG_ABSTRACT) > 0;
	}

	public boolean isFinal() {
		return (value & FLAG_FINAL) > 0;
	}

	public Modifiers withFinal() {
		return new Modifiers(value | FLAG_FINAL);
	}

	public static boolean isFinal(int modifiers) {
		return (modifiers & FLAG_FINAL) > 0;
	}

	public boolean isConst() {
		return (value & FLAG_CONST) > 0;
	}

	public Modifiers withConst() {
		return new Modifiers(value | FLAG_CONST);
	}

	public static boolean isConst(int modifiers) {
		return (modifiers & FLAG_CONST) > 0;
	}

	public boolean isConstOptional() {
		return (value & FLAG_CONST_OPTIONAL) > 0;
	}

	public Modifiers withConstOptional() {
		return new Modifiers(value | FLAG_CONST_OPTIONAL);
	}

	public static boolean isConstOptional(int modifiers) {
		return (modifiers & FLAG_CONST_OPTIONAL) > 0;
	}

	public boolean isStatic() {
		return (value & FLAG_STATIC) > 0;
	}

	public Modifiers withStatic() {
		return new Modifiers(value | FLAG_STATIC);
	}

	public static boolean isStatic(int modifiers) {
		return (modifiers & FLAG_STATIC) > 0;
	}


	public boolean isImplicit() {
		return (value & FLAG_IMPLICIT) > 0;
	}

	public Modifiers withImplicit() {
		return new Modifiers(value | FLAG_IMPLICIT);
	}

	public static boolean isImplicit(int modifiers) {
		return (modifiers & FLAG_IMPLICIT) > 0;
	}

	public boolean isVirtual() {
		return (value & FLAG_VIRTUAL) > 0;
	}

	public Modifiers withVirtual() {
		return new Modifiers(value | FLAG_VIRTUAL);
	}

	public static boolean isVirtual(int modifiers) {
		return (modifiers & FLAG_VIRTUAL) > 0;
	}

	public boolean isExtern() {
		return (value & FLAG_EXTERN) > 0;
	}

	public Modifiers withExtern() {
		return new Modifiers(value | FLAG_EXTERN);
	}

	public static boolean isExtern(int modifiers) {
		return (modifiers & FLAG_EXTERN) > 0;
	}

	public static boolean isOverride(int modifiers) {
		return (modifiers & FLAG_OVERRIDE) > 0;
	}

	public boolean hasAccessModifiers() {
		return (value & (FLAG_PRIVATE | FLAG_PUBLIC | FLAG_PROTECTED | FLAG_INTERNAL)) > 0;
	}

	public static boolean hasAccess(int modifiers) {
		return (modifiers & (FLAG_PRIVATE | FLAG_PUBLIC | FLAG_PROTECTED | FLAG_INTERNAL)) > 0;
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
