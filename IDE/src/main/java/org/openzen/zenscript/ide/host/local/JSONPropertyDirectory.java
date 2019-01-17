/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import listeners.ListenerHandle;
import listeners.ListenerList;
import live.MutableLiveBool;
import org.json.JSONObject;
import org.openzen.zenscript.ide.host.IDEPropertyDirectory;
import zsynthetic.FunctionBoolBoolToVoid;

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
	public MutableLiveBool getLiveBool(String name, boolean defaultValue) {
		return new JSONLiveBool(name, defaultValue);
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
	
	private class JSONLiveBool implements MutableLiveBool {
		private final ListenerList<FunctionBoolBoolToVoid> listeners = new ListenerList<>();
		private final String key;
		private final boolean defaultValue;
		
		public JSONLiveBool(String key, boolean defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean getValue() {
			return data.optBoolean(key, defaultValue);
		}
		
		@Override
		public void setValue(boolean value) {
			boolean current = getValue();
			data.put(key, value);
			listeners.accept(listener -> listener.invoke(current, value));
		}

		@Override
		public ListenerHandle<FunctionBoolBoolToVoid> addListener(FunctionBoolBoolToVoid listener) {
			return listeners.add(listener);
		}
	}
}
