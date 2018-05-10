/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import java.io.*;
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
	private final Map<String, byte[]> classes = new HashMap<>();
	
	public JavaModule() {
		
	}
	
	public void register(String classname, byte[] bytecode) {
		if(bytecode == null) return;
		classes.put(classname, bytecode);
		try(FileOutputStream writer = new FileOutputStream(new File(classname + ".class"))) {
			writer.write(bytecode);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void execute() {
		ScriptClassLoader classLoader = new ScriptClassLoader();
		
		try {
			classLoader.loadClass("Scripts").getMethod("run").invoke(null);
		} catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | SecurityException | IllegalArgumentException ex) {
			Logger.getLogger(JavaModule.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private class ScriptClassLoader extends ClassLoader {
		private final Map<String, Class> customClasses = new HashMap<>();

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			if(customClasses.containsKey(name))
				return customClasses.get(name);
			if(classes.containsKey(name)) {
				final byte[] bytes = classes.get(name);
				customClasses.put(name, defineClass(name, bytes, 0, bytes.length, null));
				return customClasses.get(name);
			}
			return super.loadClass(name);
		}
	}
}
