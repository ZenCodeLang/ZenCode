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
public class JavaSourceClass implements Comparable<JavaSourceClass> {
	public final String pkg;
	public final String name;
	public final String fullName;
	public boolean empty = false;
	
	public JavaSourceClass(String pkg, String name) {
		this.pkg = pkg;
		this.name = name;
		this.fullName = pkg + '.' + name;
	}

	@Override
	public int compareTo(JavaSourceClass o) {
		return fullName.compareTo(o.fullName);
	}
}
