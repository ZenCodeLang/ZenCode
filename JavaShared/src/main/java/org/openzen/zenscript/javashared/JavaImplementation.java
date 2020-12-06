/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

/**
 * @author Hoofdgebruiker
 */
public class JavaImplementation {
	public final boolean inline;
	public final JavaClass implementationClass;

	public JavaImplementation(boolean inline, JavaClass implementationClass) {
		this.inline = inline;
		this.implementationClass = implementationClass;
	}
}
