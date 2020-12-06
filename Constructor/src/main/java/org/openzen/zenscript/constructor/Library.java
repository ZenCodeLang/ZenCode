/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openzen.zenscript.constructor.module.ModuleReference;
import org.openzen.zenscript.constructor.module.SourceModuleReference;
import org.openzen.zenscript.constructor.module.directory.DirectorySourceModule;

/**
 * @author Hoofdgebruiker
 */
public class Library {
	public final String name;
	public final File directory;
	public final ModuleReference[] modules;

	public Library(File directory, String name, JSONObject json) {
		this.name = name;
		this.directory = new File(directory, json.getString("directory"));

		JSONArray modulesJson = json.getJSONArray("modules");
		modules = new ModuleReference[modulesJson.length()];
		for (int i = 0; i < modulesJson.length(); i++) {
			String moduleName = modulesJson.getString(i);
			modules[i] = new SourceModuleReference(new DirectorySourceModule(moduleName, new File(this.directory, moduleName), false), false);
		}
	}
}
