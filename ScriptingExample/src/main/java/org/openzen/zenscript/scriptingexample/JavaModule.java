/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaModule {
	private Map<String, byte[]> classes = new HashMap<>();
	
	public JavaModule() {
		
	}
	
	public void register(String classname, byte[] bytecode) {
		classes.put(classname, bytecode);
	}
	
	public void execute() {
		ScriptClassLoader classLoader = new ScriptClassLoader();
		
		try {
			classLoader.loadClass("Scripts").getMethod("run").invoke(null);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(JavaModule.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(JavaModule.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SecurityException ex) {
			Logger.getLogger(JavaModule.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(JavaModule.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(JavaModule.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InvocationTargetException ex) {
			Logger.getLogger(JavaModule.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private class ScriptClassLoader extends ClassLoader {
		
	}
}
