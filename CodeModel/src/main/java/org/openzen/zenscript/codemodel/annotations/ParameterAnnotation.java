/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ParameterAnnotation {
	public static final ParameterAnnotation[] NONE = new ParameterAnnotation[0];
	
	public AnnotationDefinition getDefinition();
	
	public void apply();
}
