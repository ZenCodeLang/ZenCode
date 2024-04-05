/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 * @author Hoofdgebruiker
 */
public class JavaSynthesizedFunction {
	public final JavaClass cls;
	public final TypeParameter[] typeParameters;
	public final FunctionHeader header;
	public final String method;
	public JavaMethod javaMethod;

	public JavaSynthesizedFunction(JavaClass cls, TypeParameter[] parameters, FunctionHeader header, String method) {
		this.cls = cls;
		this.typeParameters = parameters;
		this.header = header;
		this.method = method;
	}

	public JavaSynthesizedFunction(JavaClass cls, TypeParameter[] parameters, FunctionHeader header, String method, JavaMethod javaMethod) {
		this.cls = cls;
		this.typeParameters = parameters;
		this.header = header;
		this.method = method;
		this.javaMethod = javaMethod;
	}
}
