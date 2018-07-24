/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceClass implements Comparable<JavaSourceClass> {
	public final JavaSourceClass outer;
	
	public final String pkg;
	public final String fullName;
	public final List<String> className;
	public boolean empty = false;
	public boolean destructible = false;
	
	public JavaSourceClass(String pkg, String name) {
		this.pkg = pkg;
		this.className = Collections.singletonList(name);
		this.fullName = pkg + '.' + name;
		
		outer = this;
	}
	
	public JavaSourceClass(JavaSourceClass outer, String name) {
		this.pkg = outer.pkg;
		this.className = new ArrayList<>();
		this.className.addAll(outer.className);
		this.className.add(name);
		this.fullName = outer.fullName + '.' + name;
		
		this.outer = outer.outer;
	}
	
	public String getName() {
		return className.get(className.size() - 1);
	}
	
	public String getClassName() {
		return fullName.substring(pkg.length() + 1);
	}

	@Override
	public int compareTo(JavaSourceClass o) {
		return fullName.compareTo(o.fullName);
	}
}
