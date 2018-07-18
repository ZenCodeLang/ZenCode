package org.openzen.zenscript.constructor;

import java.io.File;
import org.openzen.zenscript.compiler.CompilationUnit;
import java.io.IOException;
import java.util.function.Consumer;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.constructor.module.DirectoryModuleReference;
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
		
		Consumer<CompileException> exceptionLogger = exception -> System.err.println(exception.toString());
		
		File currentDirectory = new File(arguments.directory);
		CompilationUnit compilationUnit = new CompilationUnit();
		
		ModuleLoader moduleLoader = new ModuleLoader(compilationUnit, exceptionLogger);
		moduleLoader.register("stdlib", new DirectoryModuleReference("stdlib", new File("../../StdLibs/stdlib"), true));
		
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
