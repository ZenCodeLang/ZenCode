/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

/**
 * @author Hoofdgebruiker
 */
public class JavaFieldInfo {
	public final JavaClassInfo javaClass;
	public final String name;
	public final String signature;

	public JavaFieldInfo(JavaClassInfo javaClass, String name, String signature) {
		this.javaClass = javaClass;
		this.name = name;
		this.signature = signature;
	}
}
