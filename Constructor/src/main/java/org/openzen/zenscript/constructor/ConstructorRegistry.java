/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

import org.json.JSONObject;
import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.compiler.Target;
import org.openzen.zenscript.compiler.TargetType;
import org.openzen.zenscript.compiler.ZenCodeCompiler;
import org.openzen.zenscript.javabytecode.JavaBytecodeModule;
import org.openzen.zenscript.javabytecode.JavaBytecodeRunUnit;
import org.openzen.zenscript.javabytecode.JavaCompiler;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.SimpleJavaCompileSpace;
import org.openzen.zenscript.javasource.JavaDirectoryOutput;
import org.openzen.zenscript.javasource.JavaSourceCompiler;
import org.openzen.zenscript.javasource.JavaSourceModule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hoofdgebruiker
 */
public class ConstructorRegistry {
	private static final Map<String, TargetType> targetTypes = new HashMap<>();

	static {
		registerTargetType("javaSource", new JavaSourceTargetType());
		registerTargetType("javaBytecodeJar", new JavaBytecodeTargetType());
		registerTargetType("javaBytecodeRun", new JavaBytecodeTargetType());
	}

	private ConstructorRegistry() {
	}

	public static void registerTargetType(String name, TargetType type) {
		targetTypes.put(name, type);
	}

	public static TargetType getTargetType(String name) {
		return targetTypes.get(name);
	}

	private static class JavaSourceTargetType implements TargetType {

		@Override
		public Target create(File projectDir, JSONObject definition) {
			return new JavaSourceTarget(projectDir, definition);
		}
	}

	private static class JavaSourceTarget implements Target {
		private final String module;
		private final String name;
		private final File output;

		public JavaSourceTarget(File projectDir, JSONObject definition) {
			module = definition.getString("module");
			name = definition.optString("name", "Java Source: " + module);
			output = new File(projectDir, definition.getString("output"));
		}

		@Override
		public ZenCodeCompiler createCompiler(SemanticModule module, IZSLogger logger) {
			return new JavaSourceZenCompiler(output, module.registry, logger);
		}

		@Override
		public String getModule() {
			return module;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean canRun() {
			return false;
		}

		@Override
		public boolean canBuild() {
			return true;
		}
	}

	private static class JavaSourceZenCompiler implements ZenCodeCompiler {
		public final GlobalTypeRegistry registry;
		private final File output;
		private final JavaSourceCompiler compiler;
		private final List<JavaSourceModule> modules = new ArrayList<>();
		private final SimpleJavaCompileSpace space;
		private final IZSLogger logger;

		public JavaSourceZenCompiler(File output, GlobalTypeRegistry registry, IZSLogger logger) {
			this.output = output;
			compiler = new JavaSourceCompiler(registry);
			this.registry = registry;
			space = new SimpleJavaCompileSpace(registry);
			this.logger = logger;
		}

		@Override
		public void addModule(SemanticModule module) {
			JavaSourceModule result = compiler.compile(logger, module, space, module.modulePackage.fullName);
			writeMappings(result);

			modules.add(result);
			space.register(result);
		}

		@Override
		public void finish() {
			JavaDirectoryOutput output = new JavaDirectoryOutput(this.output);
			for (JavaSourceModule module : modules)
				output.add(module);
			output.add(compiler.helpers);
		}

		@Override
		public void run() {
			throw new UnsupportedOperationException();
		}

		private void writeMappings(JavaCompiledModule module) {
			String mappings = module.generateMappings();
			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(output, "java.map")), StandardCharsets.UTF_8)) {
				writer.write(mappings);
			} catch (IOException ex) {

			}
		}
	}

	private static class JavaBytecodeTargetType implements TargetType {

		@Override
		public Target create(File projectDir, JSONObject definition) {
			return new JavaBytecodeJarTarget(definition);
		}
	}

	private static class JavaBytecodeJarTarget implements Target {
		private final String module;
		private final String name;
		private final File file;
		private final boolean debugCompiler;

		public JavaBytecodeJarTarget(JSONObject definition) {
			module = definition.getString("module");
			name = definition.getString("name");
			file = new File(definition.getString("output"));
			debugCompiler = definition.optBoolean("debugCompiler", false);
		}

		@Override
		public ZenCodeCompiler createCompiler(SemanticModule module, IZSLogger logger) {
			return new JavaBytecodeJarCompiler(logger);
		}

		@Override
		public String getModule() {
			return module;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean canRun() {
			return true;
		}

		@Override
		public boolean canBuild() {
			return true;
		}
	}

	private static class JavaBytecodeJarCompiler implements ZenCodeCompiler {
		private final ZSPackage root = ZSPackage.createRoot();
		private final ZSPackage stdlib = new ZSPackage(root, "stdlib");
		public final GlobalTypeRegistry registry = new GlobalTypeRegistry(stdlib);

		private final JavaCompiler compiler;
		private final List<JavaBytecodeModule> modules = new ArrayList<>();
		private final SimpleJavaCompileSpace space = new SimpleJavaCompileSpace(registry);
		private final IZSLogger logger;

		public JavaBytecodeJarCompiler(IZSLogger logger) {
			this.logger = logger;
			compiler = new JavaCompiler(logger);
		}

		@Override
		public void addModule(SemanticModule module) {
			JavaBytecodeModule result = compiler.compile(module.modulePackage.fullName, module, space);
			modules.add(result);
			space.register(result);
		}

		@Override
		public void finish() {

		}

		@Override
		public void run() {
			JavaBytecodeRunUnit unit = new JavaBytecodeRunUnit(logger);
			for (JavaBytecodeModule module : modules)
				unit.add(module);
			//unit.add(compiler.helpers);
			try {
				unit.run();
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
}
