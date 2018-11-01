/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openzen.zenscript.javabytecode.compiler.JavaClassWriter;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaMethod;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaBytecodeRunUnit {
	private final Map<String, byte[]> classes = new HashMap<>();
	private final List<JavaMethod> scripts = new ArrayList<>();
	
	private boolean scriptsWritten = false;
	
	public void add(JavaBytecodeModule module) {
		scriptsWritten = false;
		
		for (Map.Entry<String, byte[]> classEntry : module.getClasses().entrySet())
			classes.put(classEntry.getKey().replace('/', '.'), classEntry.getValue());
		
		scripts.addAll(module.getScripts());
	}
	
	public void run() {
		if (!scriptsWritten)
			writeScripts();
		
		ScriptClassLoader classLoader = new ScriptClassLoader();

		try {
			classLoader.loadClass("Scripts").getMethod("run").invoke(null);
		} catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | SecurityException | IllegalArgumentException ex) {
			Logger.getLogger(JavaModule.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private void writeScripts() {
		JavaClassWriter scriptsClassWriter = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
		scriptsClassWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Scripts", null, "java/lang/Object", null);
		
		JavaMethod runMethod = JavaMethod.getStatic(new JavaClass("script", "Scripts", JavaClass.Kind.CLASS), "run", "()V", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
		final JavaWriter runWriter = new JavaWriter(scriptsClassWriter, runMethod, null, null, null);
		runWriter.start();
		for (JavaMethod method : scripts)
			runWriter.invokeStatic(method);
		
		runWriter.ret();
		runWriter.end();
		
		classes.put("Scripts", scriptsClassWriter.toByteArray());
	}

	public class ScriptClassLoader extends ClassLoader {
		private final Map<String, Class> customClasses = new HashMap<>();

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			//System.out.println("LoadClass " + name);
			
			if (customClasses.containsKey(name))
				return customClasses.get(name);
			if (classes.containsKey(name)) {
				final byte[] bytes = classes.get(name);
				customClasses.put(name, defineClass(name, bytes, 0, bytes.length, null));
				return customClasses.get(name);
			}
			return super.loadClass(name);
		}
	}
}
