/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.Expression;

/**
 * @author Hoofdgebruiker
 */
public class JavaNativeField implements JavaField {
	public final JavaClass cls;
	public final String name;
	public final String descriptor;
	public final String signature; // TODO: calculate signature too

	public JavaNativeField(JavaClass cls, String name, String descriptor) {
		this(cls, name, descriptor, null);
	}

	public JavaNativeField(JavaClass cls, String name, String descriptor, String signature) {
		this.cls = cls;
		this.name = name;
		this.descriptor = descriptor;
		this.signature = signature;
	}

	@Override
	public String getMapping(JavaClass definition) {
		StringBuilder result = new StringBuilder();
		result.append(name);
		result.append(':');
		result.append(descriptor);

		if (!cls.internalName.equals(definition.internalName))
			result.append(definition.internalName);

		return result.toString();
	}

	@Override
	public <T> T compileInstanceGet(JavaFieldCompiler<T> compiler, Expression target) {
		return compiler.nativeInstanceGet(this, target);
	}

	@Override
	public <T> T compileInstanceSet(JavaFieldCompiler<T> compiler, Expression target, Expression value) {
		return compiler.nativeInstanceSet(this, target, value);
	}

	@Override
	public <T> T compileStaticGet(JavaFieldCompiler<T> compiler) {
		return compiler.nativeStaticGet(this);
	}

	@Override
	public <T> T compileStaticSet(JavaFieldCompiler<T> compiler, Expression value) {
		return compiler.nativeStaticSet(this, value);
	}
}
