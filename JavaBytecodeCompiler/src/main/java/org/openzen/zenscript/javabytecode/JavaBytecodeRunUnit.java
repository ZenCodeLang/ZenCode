/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.javabytecode.compiler.JavaClassWriter;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaParameterInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Hoofdgebruiker
 */
public class JavaBytecodeRunUnit {
	private final Map<String, byte[]> classes = new HashMap<>();
	private final List<JavaScriptMethod> scripts = new ArrayList<>();
	private final List<FunctionParameter> scriptParameters = new ArrayList<>();
	private final List<JavaParameterInfo> scriptParameterInfo = new ArrayList<>();

	private boolean scriptsWritten = false;

	public void add(JavaBytecodeModule module) {
		scriptsWritten = false;

		for (Map.Entry<String, byte[]> classEntry : module.getClasses().entrySet())
			classes.put(classEntry.getKey().replace('/', '.'), classEntry.getValue());

		for (JavaScriptMethod script : module.getScripts()) {
			scripts.add(script);

			for (int i = 0; i < script.parameters.length; i++) {
				FunctionParameter parameter = script.parameters[i];
				if (!scriptParameters.contains(parameter)) {
					scriptParameters.add(parameter);
					scriptParameterInfo.add(script.parametersInfo[i]);
				}
			}
		}
	}

	public void run() {
		run(Collections.emptyMap(), this.getClass().getClassLoader());
	}

	public void run(Map<FunctionParameter, Object> arguments) {
		run(arguments, this.getClass().getClassLoader());
	}

	public void run(Map<FunctionParameter, Object> arguments, ClassLoader parentClassLoader) {
		writeScripts();

		ScriptClassLoader classLoader = new ScriptClassLoader(parentClassLoader);

		Object[] argumentsArray = new Object[scriptParameters.size()];
		for (int i = 0; i < scriptParameters.size(); i++) {
			FunctionParameter parameter = scriptParameters.get(i);
			if (!arguments.containsKey(parameter))
				throw new IllegalArgumentException("Missing script argument for parameter " + parameter.name);

			argumentsArray[i] = arguments.get(parameter);
		}
		try {
			Class[] classes = new Class[scriptParameters.size()];
			for (int i = 0; i < classes.length; i++)
				classes[i] = loadClass(classLoader, scriptParameterInfo.get(i).typeDescriptor);
			classLoader.loadClass("Scripts").getMethod("run", classes).invoke(null, argumentsArray);
		} catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | SecurityException | IllegalArgumentException ex) {
			Logger.getLogger(JavaBytecodeRunUnit.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void dump(File directory) {
		writeScripts();

		if (!directory.exists())
			directory.mkdirs();

		for (Map.Entry<String, byte[]> classEntry : classes.entrySet()) {
			File output = new File(directory, classEntry.getKey() + ".class");
			try (FileOutputStream outputStream = new FileOutputStream(output)) {
				outputStream.write(classEntry.getValue());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private int getParameterIndex(FunctionParameter parameter) {
		return scriptParameters.indexOf(parameter);
	}

	private void writeScripts() {
		if (scriptsWritten)
			return;

		JavaClassWriter scriptsClassWriter = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
		scriptsClassWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Scripts", null, "java/lang/Object", null);

		FunctionHeader header = new FunctionHeader(BasicTypeID.VOID, scriptParameters.toArray(new FunctionParameter[scriptParameters.size()]));
		StringBuilder headerBuilder = new StringBuilder();
		headerBuilder.append('(');
		for (int i = 0; i < scriptParameters.size(); i++) {
			headerBuilder.append(scriptParameterInfo.get(i).typeDescriptor);
		}
		headerBuilder.append(")V");

		JavaMethod runMethod = JavaMethod.getStatic(new JavaClass("script", "Scripts", JavaClass.Kind.CLASS), "run", headerBuilder.toString(), Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
		final JavaWriter runWriter = new JavaWriter(CodePosition.GENERATED, scriptsClassWriter, runMethod, null, null, null);
		runWriter.start();
		for (JavaScriptMethod method : scripts) {
			for (int i = 0; i < method.parameters.length; i++) {
				runWriter.load(Type.getType(method.parametersInfo[i].typeDescriptor), getParameterIndex(method.parameters[i]));
			}
			runWriter.invokeStatic(method.method);
		}

		runWriter.ret();
		runWriter.end();

		classes.put("Scripts", scriptsClassWriter.toByteArray());
		scriptsWritten = true;
	}

	public class ScriptClassLoader extends ClassLoader {
		private final Map<String, Class> customClasses = new HashMap<>();

		public ScriptClassLoader(ClassLoader parent) {
			super(parent);
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			if (customClasses.containsKey(name))
				return customClasses.get(name);
			if (classes.containsKey(name)) {
				final byte[] bytes = classes.get(name);
				customClasses.put(name, defineClass(name, bytes, 0, bytes.length, null));
				return customClasses.get(name);
			}
			return getParent().loadClass(name);
		}
	}


	private static Class<?> loadClass(ClassLoader classLoader, String descriptor) throws ClassNotFoundException {
		switch (descriptor) {
			case "Z": return boolean.class;
			case "B": return byte.class;
			case "S": return short.class;
			case "I": return int.class;
			case "J": return long.class;
			case "F": return float.class;
			case "D": return double.class;
			case "C": return char.class;
			case "Ljava/lang/Object;": return Object.class;
			case "Ljava/lang/String;": return String.class;
			case "[Ljava/lang/Object;": return Object[].class;
			case "[Ljava/lang/String;": return String[].class;
		}

		return classLoader.loadClass(getClassName(descriptor));
	}

	private static String getClassName(String descriptor) {
		if (descriptor.startsWith("[")) {
			return "[" + getClassName(descriptor.substring(1));
		} else if (descriptor.startsWith("L")) {
			return descriptor.substring(1, descriptor.length() - 1);
		} else {
			throw new IllegalArgumentException("Invalid descriptor: " + descriptor);
		}
	}
}
