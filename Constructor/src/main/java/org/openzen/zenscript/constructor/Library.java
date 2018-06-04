/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openzen.zenscript.constructor.module.DirectoryModuleLoader;
import org.openzen.zenscript.constructor.module.ModuleReference;

/**
 *
 * @author Hoofdgebruiker
 */
public class Library {
	public final String name;
	public final File directory;
	public final ModuleReference[] modules;
	
	public Library(ModuleLoader loader, File directory, String name, JSONObject json) {
		this.name = name;
		this.directory = new File(directory, json.getString("directory"));
		
		JSONArray modulesJson = json.getJSONArray("modules");
		modules = new ModuleReference[modulesJson.length()];
		for (int i = 0; i < modulesJson.length(); i++) {
			String moduleName = modulesJson.getString(i);
			modules[i] = new DirectoryModuleLoader(loader, moduleName, new File(this.directory, moduleName), false);
		}
	}
}
