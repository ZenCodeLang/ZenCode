/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.constructor.module.DirectoryModuleReference;
import org.openzen.zenscript.constructor.module.ModuleReference;
import org.openzen.zenscript.compiler.Target;
import org.openzen.zenscript.compiler.TargetType;

/**
 *
 * @author Hoofdgebruiker
 */
public class Project {
	public final File directory;
	public final String name;
	public final ModuleReference[] modules;
	public final Library[] libraries;
	public final Target[] targets;
	
	public Project(File directory) throws IOException {
		this.directory = directory;
		name = directory.getName();
		
		if (!directory.exists())
			throw new ConstructorException("Project directory doesn't exist");
		if (!directory.isDirectory())
			throw new ConstructorException("Project directory isn't a directory");
		
		File projectFile = new File(directory, "project.json");
		if (!projectFile.exists())
			throw new ConstructorException("Missing project.json file in project directory");
		
		JSONObject json = JSONUtils.load(projectFile);
		
		JSONObject jsonLibraries = json.getJSONObject("libraries");
		libraries = new Library[jsonLibraries.length()];
		int k = 0;
		for (String key : jsonLibraries.keySet()) {
			libraries[k] = new Library(directory, key, jsonLibraries.getJSONObject(key));
			k++;
		}
		
		JSONArray jsonModules = json.getJSONArray("modules");
		modules = new ModuleReference[jsonModules.length()];
		for (int i = 0; i < jsonModules.length(); i++) {
			Object module = jsonModules.get(i);
			if (module instanceof String) {
				modules[i] = new DirectoryModuleReference(module.toString(), new File(directory, module.toString()), false);
			} else if (module instanceof JSONObject) {
				JSONObject jsonModule = (JSONObject) module;
				String name = jsonModule.getString("name");
				switch (jsonModule.getString("type")) {
					case "directory":
						modules[i] = new DirectoryModuleReference(name, new File(directory, jsonModule.getString("directory")), false);
						break;
					default:
						throw new ConstructorException("Invalid module type: " + jsonModule.getString("type"));
				}
			}
		}
		
		JSONArray jsonTargets = json.getJSONArray("targets");
		List<Target> targetList = new ArrayList<>();
		for (int i = 0; i < jsonTargets.length(); i++) {
			JSONObject jsonTarget = jsonTargets.getJSONObject(i);
			TargetType targetType = ConstructorRegistry.getTargetType(jsonTarget.getString("type"));
			if (targetType == null) {
				System.out.println("Unknown target type: " + jsonTarget.getString("type"));
				continue;
			}
			
			targetList.add(targetType.create(directory, jsonTarget));
		}
		targets = targetList.toArray(new Target[targetList.size()]);
	}
}
