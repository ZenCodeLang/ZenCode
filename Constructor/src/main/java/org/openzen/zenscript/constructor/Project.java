/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

import java.io.File;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openzen.zenscript.constructor.module.DirectoryModuleLoader;
import org.openzen.zenscript.constructor.module.ModuleReference;

/**
 *
 * @author Hoofdgebruiker
 */
public class Project {
	public final String name;
	public final ModuleReference[] modules;
	public final Library[] libraries;
	
	public Project(ModuleLoader loader, File directory) throws IOException {
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
			libraries[k] = new Library(loader, directory, key, jsonLibraries.getJSONObject(key));
			k++;
		}
		
		JSONArray jsonModules = json.getJSONArray("modules");
		modules = new ModuleReference[jsonModules.length()];
		for (int i = 0; i < jsonModules.length(); i++) {
			Object module = jsonModules.get(i);
			if (module instanceof String) {
				modules[i] = new DirectoryModuleLoader(loader, module.toString(), new File(directory, module.toString()), false);
			} else if (module instanceof JSONObject) {
				JSONObject jsonModule = (JSONObject) module;
				String name = jsonModule.getString("name");
				switch (jsonModule.getString("type")) {
					case "directory":
						modules[i] = new DirectoryModuleLoader(loader, name, new File(directory, jsonModule.getString("directory")), false);
						break;
					default:
						throw new ConstructorException("Invalid module type: " + jsonModule.getString("type"));
				}
			}
		}
	}
}
