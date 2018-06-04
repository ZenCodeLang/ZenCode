/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.json.JSONObject;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.compiler.Target;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaBytecodeRunTarget implements Target {
	private final String module;
	private final String name;
	private final boolean debugCompiler;
	
	public JavaBytecodeRunTarget(JSONObject definition) {
		module = definition.getString("module");
		name = definition.optString("name", "Java Run: " + module);
		debugCompiler = definition.optBoolean("debugCompiler", false);
	}
	
	@Override
	public JavaCompiler createCompiler(SemanticModule module) {
		return new JavaCompiler(debugCompiler);
	}

	@Override
	public String getModule() {
		return module;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean canRun() {
		return true;
	}

	@Override
	public boolean canBuild() {
		return true;
	}
}
