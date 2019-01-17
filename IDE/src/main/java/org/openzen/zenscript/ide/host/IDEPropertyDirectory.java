/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host;

import live.MutableLiveBool;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDEPropertyDirectory {
	public boolean getBool(String name, boolean defaultValue);
	
	public void setBool(String name, boolean value);
	
	public MutableLiveBool getLiveBool(String name, boolean defaultValue);
	
	public int getInt(String name, int defaultValue);
	
	public void setInt(String name, int value);
	
	public String getString(String name, String defaultValue);
	
	public void setString(String name, String value);
	
	public IDEPropertyDirectory getSubdirectory(String name);
}
