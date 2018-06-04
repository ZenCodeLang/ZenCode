package org.openzen.zenscript.constructor;

import java.io.File;
import java.io.IOException;
import org.openzen.zenscript.constructor.module.DirectoryModuleLoader;
import org.openzen.zenscript.constructor.module.ModuleReference;

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
		
		File currentDirectory = new File(arguments.directory);
		ModuleLoader moduleLoader = new ModuleLoader();
		moduleLoader.register("stdlib", new DirectoryModuleLoader(moduleLoader, "stdlib", new File("libraries/stdlib"), true));
		
		Project project = new Project(moduleLoader, currentDirectory);
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
