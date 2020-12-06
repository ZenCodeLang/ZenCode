/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 * @author Hoofdgebruiker
 */
public class JavaSynthesizedFunctionInstance {
	public final TypeID[] typeArguments;
	private final JavaSynthesizedFunction function;

	public JavaSynthesizedFunctionInstance(JavaSynthesizedFunction function, TypeID[] typeArguments) {
		this.function = function;
		this.typeArguments = typeArguments;
	}

	public JavaClass getCls() {
		return function.cls;
	}

	public String getMethod() {
		return function.method;
	}

	public FunctionHeader getHeader() {
		return function.header;
	}
}
