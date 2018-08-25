/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import org.openzen.zenscript.compiler.Target;
import org.openzen.zenscript.compiler.ZenCodeCompiler;
import org.openzen.zenscript.constructor.Library;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.constructor.Project;
import org.openzen.zenscript.constructor.module.DirectoryModuleReference;
import org.openzen.zenscript.constructor.module.ModuleReference;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.ide.host.IDETarget;
import org.openzen.zenscript.ide.ui.view.output.ErrorOutputSpan;
import org.openzen.zenscript.ide.ui.view.output.OutputLine;
import org.openzen.zenscript.validator.ValidationLogEntry;
import stdlib.Strings;

/**
 *
 * @author Hoofdgebruiker
 */
public class LocalTarget implements IDETarget {
	private final Project project;
	private final Target target;
	
	public LocalTarget(Project project, Target target) {
		this.project = project;
		this.target = target;
	}

	@Override
	public String getName() {
		return target.getName();
	}

	@Override
	public boolean canBuild() {
		return target.canBuild();
	}

	@Override
	public boolean canRun() {
		return target.canRun();
	}

	@Override
	public void build(Consumer<OutputLine> output) {
		buildInternal(output);
	}

	@Override
	public void run(Consumer<OutputLine> output) {
		ZenCodeCompiler compiler = buildInternal(output);
		if (compiler != null)
			compiler.run();
	}
	
	private ZenCodeCompiler buildInternal(Consumer<OutputLine> output) {
		CompilationUnit compilationUnit = new CompilationUnit();
		ModuleLoader moduleLoader = new ModuleLoader(compilationUnit, exception -> {
			String[] lines = Strings.split(exception.getMessage(), '\n');
			for (String line : lines) {
				output.accept(new OutputLine(new ErrorOutputSpan(line)));
			}
		});
		moduleLoader.register("stdlib", new DirectoryModuleReference("stdlib", new File("../../StdLibs/stdlib"), true));
		Set<String> compiledModules = new HashSet<>();
		
		Consumer<ValidationLogEntry> validationLogger = entry -> {
			String[] message = Strings.split(entry.message, '\n');
			output.accept(new OutputLine(new ErrorOutputSpan(entry.kind + " " + entry.position.toString() + ": " + message[0])));
			for (int i = 1; i < message.length; i++)
				output.accept(new OutputLine(new ErrorOutputSpan("    " + message[i])));
		};
		try {
			for (Library library : project.libraries) {
				for (ModuleReference module : library.modules)
					moduleLoader.register(module.getName(), module);
			}
			for (ModuleReference module : project.modules) {
				moduleLoader.register(module.getName(), module);
			}
			
			SemanticModule module = moduleLoader.getModule(target.getModule());
			module = module.normalize();
			module.validate(validationLogger);
			
			ZenCodeCompiler compiler = target.createCompiler(module);
			if (!module.isValid())
				return compiler;
			
			SemanticModule stdlib = moduleLoader.getModule("stdlib");
			stdlib = stdlib.normalize();
			stdlib.validate(validationLogger);
			if (!stdlib.isValid())
				return compiler;
			stdlib.compile(compiler);
			compiledModules.add(stdlib.name);
			
			boolean isValid = compileDependencies(moduleLoader, compiler, compiledModules, new Stack<>(), module, validationLogger);
			if (!isValid)
				return compiler;
			
			module.compile(compiler);
			compiler.finish();
			return compiler;
		} catch (Exception ex) {
			ex.printStackTrace();
			
			for (String line : Strings.split(ex.toString(), '\n'))
				output.accept(new OutputLine(new ErrorOutputSpan(line)));
			for (StackTraceElement element : ex.getStackTrace()) {
				String source;
				if (element.isNativeMethod()) {
					source = "(Native Method)";
				} else {
					source = "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
				}
				output.accept(new OutputLine(new ErrorOutputSpan("    at " + element.getClassName() + "." + element.getMethodName() + source)));
			}
			
			return null;
		}
	}
	
	private boolean compileDependencies(ModuleLoader loader, ZenCodeCompiler compiler, Set<String> compiledModules, Stack<String> compilingModules, SemanticModule module, Consumer<ValidationLogEntry> logger) {
		for (SemanticModule dependency : module.dependencies) {
			if (compiledModules.contains(dependency.name))
				continue;
			compiledModules.add(dependency.name);
			System.out.println("== Compiling module " + dependency.name + " ==");
			
			if (compilingModules.contains(dependency.name)) {
				StringBuilder message = new StringBuilder("Circular dependency:\n");
				for (String s : compilingModules)
					message.append("    ").append(s).append("\n");
				
				throw new IllegalStateException(message.toString());
			}
			compilingModules.push(dependency.name);
			
			if (!dependency.isValid()) {
				compilingModules.pop();
				return false;
			}
			
			dependency = dependency.normalize();
			dependency.validate(logger);
			if (!dependency.isValid()) {
				compilingModules.pop();
				return false;
			}
			
			if (!compileDependencies(loader, compiler, compiledModules, compilingModules, dependency, logger)) {
				compilingModules.pop();
				return false;
			}
			
			dependency.compile(compiler);
			compilingModules.pop();
		}
		
		return true;
	}
}
