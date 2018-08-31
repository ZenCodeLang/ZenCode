/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSynthesizedRange {
	public final JavaClass cls;
	public final TypeParameter[] typeParameters;
	public final ITypeID baseType;
	
	public JavaSynthesizedRange(JavaClass cls, TypeParameter[] typeParameters, ITypeID baseType) {
		this.cls = cls;
		this.typeParameters = typeParameters;
		this.baseType = baseType;
	}
}
