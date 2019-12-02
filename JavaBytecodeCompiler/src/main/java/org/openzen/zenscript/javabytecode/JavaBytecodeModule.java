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
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaMethod;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaBytecodeModule extends JavaCompiledModule {
	private final Map<String, byte[]> classes = new HashMap<>();
	private final List<JavaScriptMethod> scripts = new ArrayList<>();
	
	public JavaBytecodeModule(Module module, FunctionParameter[] parameters) {
		super(module, parameters);
	}
	
	public void addClass(String name, byte[] bytecode) {
		if (bytecode == null)
			return;

		if(name.startsWith("java")) {
			System.err.println("Invalid name " + name);
		} else {
			classes.put(name, bytecode);
		}
	}
	
	public void addScript(JavaScriptMethod method) {
		scripts.add(method);
	}
	
	public Map<String, byte[]> getClasses() {
		return classes;
	}
	
	public List<JavaScriptMethod> getScripts() {
		return scripts;
	}
}
