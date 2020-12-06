/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaParameterInfo;

/**
 * @author Hoofdgebruiker
 */
public class JavaScriptMethod {
	public final JavaMethod method;
	public final FunctionParameter[] parameters;
	public final JavaParameterInfo[] parametersInfo;

	public JavaScriptMethod(JavaMethod method, FunctionParameter[] parameters, JavaParameterInfo[] parametersInfo) {
		this.method = method;
		this.parameters = parameters;
		this.parametersInfo = parametersInfo;
	}
}
