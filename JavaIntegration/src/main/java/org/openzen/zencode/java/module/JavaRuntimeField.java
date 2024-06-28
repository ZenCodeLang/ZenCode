package org.openzen.zencode.java.module;

import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaNativeField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class JavaRuntimeField implements FieldSymbol {
	private final TypeSymbol definingType;
	private final String name;
	public final JavaNativeField nativeField;
	private final TypeID type;
	private final Modifiers modifiers;
	private final Field field;

	public JavaRuntimeField(TypeSymbol definingType, String name, JavaNativeField nativeField, TypeID type, Field field) {
		this.definingType = definingType;
		this.name = name;
		this.nativeField = nativeField;
		this.field = field;
		this.type = type;

		Modifiers modifiers = Modifiers.PUBLIC;
		if (Modifier.isStatic(field.getModifiers()))
			modifiers = modifiers.withStatic();

		this.modifiers = modifiers;
	}

	@Override
	public TypeSymbol getDefiningType() {
		return definingType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TypeID getType() {
		return type;
	}

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	public boolean isEnumConstant() {
		return field.isEnumConstant();
	}
}
