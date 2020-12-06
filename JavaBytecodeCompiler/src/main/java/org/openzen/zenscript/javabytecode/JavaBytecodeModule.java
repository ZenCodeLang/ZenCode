/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.javashared.JavaCompiledModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hoofdgebruiker
 */
public class JavaBytecodeModule extends JavaCompiledModule {
	private final Map<String, byte[]> classes = new HashMap<>();
	private final List<JavaScriptMethod> scripts = new ArrayList<>();
	private final IZSLogger logger;

	public JavaBytecodeModule(Module module, FunctionParameter[] parameters, IZSLogger logger) {
		super(module, parameters);
		this.logger = logger;
	}

	public void addClass(String name, byte[] bytecode) {
		if (bytecode == null)
			return;

		if (name.startsWith("java")) {
			logger.trace("Warning: Invalid name " + name);
		} else if (classes.containsKey(name)) {
			logger.warning("Trying to register " + name + " a 2nd time");
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
