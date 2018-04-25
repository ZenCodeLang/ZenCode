package org.openzen.zenscript.scriptingexample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.StatementVisitor;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.linker.symbol.ISymbol;
import org.openzen.zenscript.parser.ParsedFile;
import org.openzen.zenscript.scriptingexample.writer.JavaStatementVisitor;
import org.openzen.zenscript.scriptingexample.writer.JavaWriter;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
		System.out.println();
		File inputDirectory = new File("scripts");
		File[] inputFiles = Optional.ofNullable(inputDirectory.listFiles((dir, name) -> name.endsWith(".zs"))).orElseGet(() -> new File[0]);
		
		ParsedFile[] parsedFiles = parse(inputFiles);
		
		GlobalRegistry registry = new GlobalRegistry();
		SemanticModule module = compileSyntaxToSemantic(parsedFiles, registry);
		
		JavaModule javaModule = compileSemanticToJava(module);
		javaModule.execute();
    }
	
	private static ParsedFile[] parse(File[] files) throws IOException {
		ParsedFile[] parsedFiles = new ParsedFile[files.length];
		for (int i = 0; i < files.length; i++) {
			parsedFiles[i] = ParsedFile.parse(files[i]);
		}
		return parsedFiles;
	}
	
	private static SemanticModule compileSyntaxToSemantic(ParsedFile[] files, GlobalRegistry registry) {
		// We are considering all these files to be in the same package, so make
		// a single PackageDefinition instance. If these files were in multiple
		// packages, we'd need an instance for every package.
		PackageDefinitions definitions = new PackageDefinitions();
		for (ParsedFile file : files) {
			// listDefinitions will merely register all definitions (classes,
			// interfaces, functions ...) so they can later be available to
			// the other files as well. It doesn't yet compile anything.
			file.listDefinitions(definitions);
		}
		
		ZSPackage rootPackage = registry.collectPackages();
		List<ExpansionDefinition> expansions = registry.collectExpansions();
		Map<String, ISymbol> globals = registry.collectGlobals();
		
		GlobalTypeRegistry globalRegistry = new GlobalTypeRegistry();
		for (ParsedFile file : files) {
			// compileMembers will register all definition members to their
			// respective definitions, such as fields, constructors, methods...
			// It doesn't yet compile the method contents.
			file.compileMembers(rootPackage, definitions, globalRegistry, expansions, globals);
		}
		
		// scripts will store all the script blocks encountered in the files
		List<ScriptBlock> scripts = new ArrayList<>();
		for (ParsedFile file : files) {
			// compileCode will convert the parsed statements and expressions
			// into semantic code. This semantic code can then be compiled
			// to various targets.
			file.compileCode(rootPackage, definitions, globalRegistry, expansions, scripts, globals);
		}
		
		return new SemanticModule(definitions, scripts);
	}
	
	private static JavaModule compileSemanticToJava(SemanticModule module) {
		JavaModule result = new JavaModule();
		final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Scripts", null, "java/lang/Object", null);

		// TODO: java bytecode compilation
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			// convert definitions into java classes

		}
		int statementNo = 0;
		for (ScriptBlock script : module.scripts) {
			// convert scripts into methods (add them to a Scripts class?)
			// (TODO: can we break very long scripts into smaller methods? for the extreme scripts)
			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(new JavaWriter(classWriter, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, makeName(statementNo++), "()V", null, null));
			statementVisitor.start();
			for (Statement statement : script.statements) {
				statement.accept(statementVisitor);
			}
			statementVisitor.end();
		}

		// create a Scripts.run() method to run all scripts compiled above
		final JavaWriter runWriter = new JavaWriter(classWriter, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "()V", null, null);
		runWriter.start();
		for (int i = 0; i < statementNo; i++) {
			runWriter.invokeStatic("Scripts", makeName(i), "()V");
		}
		runWriter.ret();
		runWriter.end();
		result.register("Scripts", classWriter.toByteArray());

		
		return result;
	}

	private static String makeName(int i) {
    	return "scriptBlock" + i;
	}
}
