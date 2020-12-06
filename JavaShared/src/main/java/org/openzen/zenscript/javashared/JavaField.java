/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

/**
 * @author Hoofdgebruiker
 */
public class JavaField {
	public final JavaClass cls;
	public final String name;
	public final String descriptor;
	public final String signature; // TODO: calculate signature too

	public JavaField(JavaClass cls, String name, String descriptor) {
		this(cls, name, descriptor, null);
	}

	public JavaField(JavaClass cls, String name, String descriptor, String signature) {
		this.cls = cls;
		this.name = name;
		this.descriptor = descriptor;
		this.signature = signature;
	}

	public String getMapping(JavaClass definition) {
		StringBuilder result = new StringBuilder();
		result.append(name);
		result.append(':');
		result.append(descriptor);

		if (!cls.internalName.equals(definition.internalName))
			result.append(definition.internalName);

		return result.toString();
	}
}
