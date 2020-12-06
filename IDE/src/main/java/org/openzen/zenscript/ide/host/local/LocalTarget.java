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

import live.LiveObject;
import live.SimpleLiveObject;
import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.compiler.Target;
import org.openzen.zenscript.compiler.ZenCodeCompiler;
import org.openzen.zenscript.constructor.Library;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.constructor.Project;
import org.openzen.zenscript.constructor.module.directory.DirectorySourceModule;
import org.openzen.zenscript.constructor.module.ModuleReference;
import org.openzen.zenscript.constructor.module.SourceModuleReference;
import org.openzen.zenscript.constructor.module.logging.*;
import org.openzen.zenscript.ide.host.IDECodeError;
import org.openzen.zenscript.ide.host.IDECompileState;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.host.IDETarget;
import org.openzen.zenscript.ide.host.local.logging.*;
import org.openzen.zenscript.ide.ui.view.output.*;
import org.openzen.zenscript.parser.logger.*;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.logger.*;
import stdlib.Strings;

/**
 * @author Hoofdgebruiker
 */
public class LocalTarget implements IDETarget {
	private final Project project;
	private final Target target;
	private final SimpleLiveObject<IDECompileState> state = new SimpleLiveObject<>(null);

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
	public LiveObject<IDECompileState> load() {
		if (state.getValue() == null)
			state.setValue(precompile());

		return state;
	}

	@Override
	public void build(Consumer<OutputLine> output) {
		buildInternal(output, new LocalCompileState(), true);
	}

	@Override
	public void run(Consumer<OutputLine> output) {
		ZenCodeCompiler compiler = buildInternal(output, new LocalCompileState(), true);
		if (compiler != null)
			compiler.run();
	}

	private IDECompileState precompile() {
		LocalCompileState result = new LocalCompileState();
		buildInternal(line -> {
		}, result, false);
		return result;
	}

	private ZenCodeCompiler buildInternal(Consumer<OutputLine> output, LocalCompileState state, boolean compile) {
		ZSPackage root = ZSPackage.createRoot();
		ZSPackage stdlibPackage = new ZSPackage(root, "stdlib");
		GlobalTypeRegistry registry = new GlobalTypeRegistry(stdlibPackage);
		final LocalModuleLogger localModuleLogger = new LocalModuleLogger(state, output);
		ModuleLoader moduleLoader = new ModuleLoader(registry, localModuleLogger);

		//moduleLoader.register("stdlib", new DirectoryModuleReference("stdlib", new File("../../StdLibs/stdlib"), true));
		moduleLoader.register("stdlib", new SourceModuleReference(new DirectorySourceModule("stdlib", new File("../../StdLibs/stdlib"), true), true));
		Set<String> compiledModules = new HashSet<>();

		try {
			for (Library library : project.libraries) {
				for (ModuleReference module : library.modules)
					moduleLoader.register(module.getName(), module);
			}
			for (ModuleReference module : project.modules) {
				moduleLoader.register(module.getName(), module);
			}

			SemanticModule module = moduleLoader.getModule(target.getModule());
			module = Validator.validate(module.normalize(), localModuleLogger);

			if (compile) {
				ZenCodeCompiler compiler = target.createCompiler(module, localModuleLogger);
				if (!module.isValid())
					return compiler;

				SemanticModule stdlib = moduleLoader.getModule("stdlib");
				stdlib = Validator.validate(stdlib.normalize(), localModuleLogger);
				if (!stdlib.isValid())
					return compiler;

				compiler.addModule(stdlib);
				compiledModules.add(stdlib.name);

				boolean isValid = compileDependencies(moduleLoader, compiler, compiledModules, new Stack<>(), module, localModuleLogger);
				if (!isValid)
					return compiler;

				compiler.addModule(module);
				compiler.finish();
				return compiler;
			} else {
				return null;
			}
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

	private boolean compileDependencies(ModuleLoader loader, ZenCodeCompiler compiler, Set<String> compiledModules, Stack<String> compilingModules, SemanticModule module, ValidatorLogger logger) {
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

			dependency = Validator.validate(dependency.normalize(), logger);
			if (!dependency.isValid()) {
				compilingModules.pop();
				return false;
			}

			if (!compileDependencies(loader, compiler, compiledModules, compilingModules, dependency, logger)) {
				compilingModules.pop();
				return false;
			}

			compiler.addModule(dependency);
			compilingModules.pop();
		}

		return true;
	}
}
