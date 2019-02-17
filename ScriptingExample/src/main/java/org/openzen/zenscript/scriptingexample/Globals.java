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
	public static TestClass[] makeArray(int amount) {
		TestClass[] result = new TestClass[amount];
		for (int i = 0; i < result.length; i++)
			result[i] = new TestClass("test " + i);
		return result;
	}
	
	@Global
	public static void println(String message) {
		System.out.println(message);
	}
	
	@Global
	public static void printMany(TestClass[] objects) {
		System.out.println(objects.length + " objects present:");
		for (TestClass object : objects)
			System.out.println("  - " + object.getName());
	}
	
	@Global
    public static void addShapedRecipe(String name, TestClass output, TestClass[][] inputs) {
		System.out.println("Added " + name);
	}
	
	@Global
	public static void floatMethod(float argument) {
		System.out.println(argument);
	}
	
	@Global
	public static void invokeFunctional(MyFunctionalInterface greeter) {
		System.out.println("doSomething: " + greeter.doSomething("world"));
	}
	
	public static TestClass bracket(String value) {
		return new TestClass(value);
	}
	
	@FunctionalInterface
	public static interface MyFunctionalInterface {
		String doSomething(String value);
	}
}
