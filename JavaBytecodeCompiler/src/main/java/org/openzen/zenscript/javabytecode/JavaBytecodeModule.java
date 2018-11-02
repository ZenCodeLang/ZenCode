/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaMethod;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaBytecodeModule extends JavaCompiledModule {
	private final Map<String, byte[]> classes = new HashMap<>();
	private final List<JavaMethod> scripts = new ArrayList<>();
	
	public JavaBytecodeModule(Module module) {
		super(module);
	}
	
	public void addClass(String name, byte[] bytecode) {
		if (bytecode == null)
			return;
		
		classes.put(name, bytecode);
	}
	
	public void addScript(JavaMethod method) {
		scripts.add(method);
	}
	
	public Map<String, byte[]> getClasses() {
		return classes;
	}
	
	public List<JavaMethod> getScripts() {
		return scripts;
	}
}
