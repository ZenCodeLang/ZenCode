package org.openzen.zenscript.constructor;

import java.io.File;
import java.io.IOException;
import org.openzen.zenscript.constructor.module.DirectoryModuleLoader;

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
		File projectJson = new File(currentDirectory, "project.json");
		if (!projectJson.exists()) {
			System.out.println("Error: not a valid project (missing project.json)");
			return;
		}
		
		ModuleLoader moduleLoader = new ModuleLoader();
		moduleLoader.register("stdlib", new DirectoryModuleLoader(moduleLoader, "stdlib", new File("libraries/stdlib"), true));
		
		Project project = new Project(projectJson);
		for (String moduleName : project.modules) {
			moduleLoader.register(moduleName, new DirectoryModuleLoader(moduleLoader, moduleName, new File(currentDirectory, moduleName), false));
		}
		
		// TODO: compile targets
		for (String moduleName : project.modules) {
			moduleLoader.getModule(moduleName);
		}
    }
	
	private static void printHelp() {
		System.out.println("Usage:");
		System.out.println("  java -jar Constructor.jar [<target>] [-d directory] [-h|--help]");
	}
}
