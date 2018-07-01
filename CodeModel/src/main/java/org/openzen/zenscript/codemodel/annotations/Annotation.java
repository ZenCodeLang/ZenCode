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
public interface Annotation {
	public static final Annotation[] NONE = new Annotation[0];
	
	public void apply();
}
