package org.openzen.zenscript.constructor;

import java.io.File;
import java.io.IOException;

import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.constructor.module.ModuleReference;
import org.openzen.zenscript.constructor.module.SourceModuleReference;
import org.openzen.zenscript.constructor.module.directory.DirectorySourceModule;
import org.openzen.zenscript.constructor.module.logging.*;

public class Main {
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {
		//Arguments arguments = Arguments.parse(args);
		Arguments arguments = new Arguments("../../ZenCode", "default");
		if (arguments.help) {
			printHelp();
			return;
		}

		ModuleLogger exceptionLogger = new EmptyModuleLogger();

		File currentDirectory = new File(arguments.directory);
		ZSPackage root = ZSPackage.createRoot();
		ZSPackage stdlib = new ZSPackage(root, "stdlib");

		ModuleLoader moduleLoader = new ModuleLoader(exceptionLogger);
		moduleLoader.register("stdlib", new SourceModuleReference(new DirectorySourceModule("stdlib", new File("../../StdLibs/stdlib"), true), true));

		Project project = new Project(currentDirectory);
		for (Library library : project.libraries) {
			for (ModuleReference module : library.modules)
				moduleLoader.register(module.getName(), module);
		}
		for (ModuleReference module : project.modules) {
			moduleLoader.register(module.getName(), module);
		}

		// TODO: compile targets
		for (ModuleReference module : project.modules) {
			moduleLoader.getModule(module.getName());
		}
	}

	private static void printHelp() {
		System.out.println("Usage:");
		System.out.println("  java -jar Constructor.jar [<target>] [-d directory] [-h|--help]");
	}
}
