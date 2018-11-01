/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.io.File;
import org.json.JSONObject;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.compiler.Target;
import org.openzen.zenscript.compiler.ZenCodeCompiler;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceTarget implements Target {
	private final String module;
	private final String name;
	private final File output;
	
	public JavaSourceTarget(File projectDir, JSONObject definition) {
		module = definition.getString("module");
		name = definition.optString("name", "Java Source: " + module);
		output = new File(projectDir, definition.getString("output"));
	}

	@Override
	public ZenCodeCompiler createCompiler(SemanticModule module) {
		return new JavaSourceCompiler(output, module.registry);
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
		return false;
	}

	@Override
	public boolean canBuild() {
		return true;
	}
}
