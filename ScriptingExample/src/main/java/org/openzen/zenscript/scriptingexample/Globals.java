/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

import org.openzen.zencode.java.ZenCodeGlobals;

/**
 *
 * @author Hoofdgebruiker
 */
public class Globals implements ZenCodeGlobals {
	private Globals() {}
	
	@Global
	public static TestClass something = new TestClass("hello");
	
	@Global
	public static void println(String message) {
		System.out.println(message);
	}
}
