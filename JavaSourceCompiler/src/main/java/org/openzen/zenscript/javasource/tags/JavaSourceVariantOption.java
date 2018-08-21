/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.tags;

import org.openzen.zenscript.javashared.JavaClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceVariantOption {
	public final JavaClass variantClass;
	public final JavaClass variantOptionClass;
	
	public JavaSourceVariantOption(JavaClass variantClass, JavaClass variantOptionClass) {
		this.variantClass = variantClass;
		this.variantOptionClass = variantOptionClass;
	}
}
