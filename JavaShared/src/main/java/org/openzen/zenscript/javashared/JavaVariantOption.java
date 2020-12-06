/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

/**
 * @author Hoofdgebruiker
 */
public class JavaVariantOption {
	public final JavaClass variantClass;
	public final JavaClass variantOptionClass;

	public JavaVariantOption(JavaClass variantClass, JavaClass variantOptionClass) {
		this.variantClass = variantClass;
		this.variantOptionClass = variantOptionClass;
	}
}
