/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import org.json.JSONObject;
import org.openzen.zenscript.ide.host.IDEPropertyDirectory;

/**
 *
 * @author Hoofdgebruiker
 */
public class JSONPropertyDirectory implements IDEPropertyDirectory {
	private final JSONObject data;
	
	public JSONPropertyDirectory(JSONObject data) {
		this.data = data;
	}
	
	@Override
	public boolean getBool(String name, boolean defaultValue) {
		return data.optBoolean(name, defaultValue);
	}
	
	@Override
	public void setBool(String name, boolean value) {
		data.put(name, value);
	}
	
	@Override
	public int getInt(String name, int defaultValue) {
		return data.optInt(name, defaultValue);
	}
	
	@Override
	public void setInt(String name, int value) {
		data.put(name, value);
	}

	@Override
	public String getString(String name, String defaultValue) {
		return data.optString(name, defaultValue);
	}
	
	@Override
	public void setString(String name, String value) {
		data.put(name, value);
	}

	@Override
	public IDEPropertyDirectory getSubdirectory(String name) {
		if (!data.has(name)) {
			data.put(name, new JSONObject());
		}
		
		return new JSONPropertyDirectory(data.getJSONObject(name));
	}
}
