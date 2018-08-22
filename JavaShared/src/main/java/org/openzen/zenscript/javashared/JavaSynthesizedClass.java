/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSynthesizedClass {
	public final JavaClass cls;
	public final TypeParameter[] typeParameters;
	
	public JavaSynthesizedClass(JavaClass cls, TypeParameter[] typeParameters) {
		this.cls = cls;
		this.typeParameters = typeParameters;
	}

	public JavaSynthesizedClass withTypeParameters(TypeParameter[] typeParameters) {
		return new JavaSynthesizedClass(cls, typeParameters);
	}
}
