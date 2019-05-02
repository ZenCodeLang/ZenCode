/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

/**
 *
 * @author Hoofdgebruiker
 */
public class TestClass implements TestInterface {
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
	
	@Method
	public TestClass wrap(String quotes) {
		return new TestClass(quotes + name + quotes);
	}
	
	@Caster(implicit = true)
	@Override
	public String toString() {
		return name;
	}

	@Override
	public String interfaceMethod() {
		return "Interface method of " + name;
	}
	
	@Override
	public String getValue() {
		return "getValue " + name;
	}
	
	@Method
	public TestGenericInterface<String> generate() {
		return () -> name;
	}
	
	@Method
	public void withDefaultParameter(String a, @OptionalString("world") String b) {
		System.out.println(a + " " + b);
	}
}
