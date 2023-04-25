/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;

/**
 * @author Hoofdgebruiker
 */
public class Globals implements ZenCodeGlobals {

	private Globals() {
	}

	@Global
	public static void println(String message) {
		System.out.println(message);
	}

	@Global
	public static void doSomething(@ZenCodeType.Optional MyFunctionalInterfaceClass functionalInterfaceClass) {
		if (functionalInterfaceClass == null) {
			println("Doing something with a null function!");
		} else {
			println("Doing something: " + functionalInterfaceClass.doSomething("Hello World"));
		}
	}

	@Global
	public static String softNullString(boolean null_) {
		return null_ ? null : "value";
	}
}
