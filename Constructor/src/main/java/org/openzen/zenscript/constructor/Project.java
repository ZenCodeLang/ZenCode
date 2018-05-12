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

/**
 *
 * @author Hoofdgebruiker
 */
public class Project {
	public final String[] modules;
	
	public Project(File projectFile) throws IOException {
		JSONObject json = JSONUtils.load(projectFile);
		
		JSONArray jsonModules = json.getJSONArray("modules");
		modules = new String[jsonModules.length()];
		for (int i = 0; i < jsonModules.length(); i++)
			modules[i] = jsonModules.getString(i);
	}
}
