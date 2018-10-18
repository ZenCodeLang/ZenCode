/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

/**
 *
 * @author Hoofdgebruiker
 */
public class Module {
	public static final Module BUILTIN = new Module("builtin");
	
	public final String name;
	
	public Module(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
