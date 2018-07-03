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
public class JavaSourceField {
	public final JavaSourceClass cls;
	public final String name;
	
	public JavaSourceField(JavaSourceClass cls, String name) {
		this.cls = cls;
		this.name = name;
	}
}
