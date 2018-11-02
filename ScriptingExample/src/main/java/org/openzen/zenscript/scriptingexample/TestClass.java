/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

import org.openzen.zencode.java.ZenCodeType;

/**
 *
 * @author Hoofdgebruiker
 */
public class TestClass implements ZenCodeType {
	private final String name;
	
	@Constructor
	public TestClass(String name) {
		this.name = name;
	}
	
	@Getter
	public String getName() {
		return name;
	}
	
	@Method
	public void dump() {
		System.out.println("TestClass " + name);
	}
}
