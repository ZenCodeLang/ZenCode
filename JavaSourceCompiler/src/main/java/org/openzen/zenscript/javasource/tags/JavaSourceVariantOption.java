/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.tags;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceVariantOption {
	public final JavaSourceClass variantClass;
	public final JavaSourceClass variantOptionClass;
	
	public JavaSourceVariantOption(JavaSourceClass variantClass, JavaSourceClass variantOptionClass) {
		this.variantClass = variantClass;
		this.variantOptionClass = variantOptionClass;
	}
}
