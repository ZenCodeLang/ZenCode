/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.ModuleSpace;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.constructor.ConstructorException;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.codemodel.type.storage.StorageType;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.ParsedFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class SourceModuleReference implements ModuleReference {
	private final SourceModule module;
	private final boolean isStdlib;
	
	public SourceModuleReference(SourceModule module) {
		this(module, false);
	}
	
	public SourceModuleReference(SourceModule module, boolean isStdlib) {
		this.module = module;
		this.isStdlib = isStdlib;
	}
	
	@Override
	public String getName() {
		return module.getName();
	}

	@Override
	public SemanticModule load(ModuleLoader loader, GlobalTypeRegistry registry, Consumer<CompileException> exceptionLogger) {
		SemanticModule[] dependencies = module.loadDependencies(loader, registry, exceptionLogger);

		ModuleSpace space = new ModuleSpace(registry, new ArrayList<>(), StorageType.getStandard());
		for (SemanticModule module : dependencies) {
			try {
				space.addModule(module.name, module);
			} catch (CompileException ex) {
				throw new ConstructorException("Error: exception during compilation", ex);
			}
		}

		ZSPackage pkg = isStdlib ? registry.stdlib : new ZSPackage(null, getName());
		Module module = new Module(getName());
		CompilingPackage compilingPackage = new CompilingPackage(pkg, module);

		ParsedFile[] parsedFiles = parse(compilingPackage, exceptionLogger);
		SemanticModule result = ParsedFile.compileSyntaxToSemantic(dependencies, compilingPackage, parsedFiles, space, FunctionParameter.NONE, exceptionLogger);
		result.globals.putAll(this.module.getGlobals(result));
		return result;
	}
	
	@Override
	public SourcePackage getRootPackage() {
		return module.getRootPackage();
	}
	
	public ParsedFile[] parse(CompilingPackage compilingPackage, Consumer<CompileException> exceptionLogger) {
		// TODO: load bracket parsers from host plugins
		List<ParsedFile> files = new ArrayList<>();
		parse(files, compilingPackage, null, module.getRootPackage(), exceptionLogger);
		return files.toArray(new ParsedFile[files.size()]);
	}
	
	private static void parse(List<ParsedFile> files, CompilingPackage pkg, BracketExpressionParser bracketParser, SourcePackage directory, Consumer<CompileException> exceptionLogger) {
		for (SourceFile file : directory.getFiles()) {
			try {
				files.add(ParsedFile.parse(pkg, bracketParser, file));
			} catch (ParseException ex) {
				exceptionLogger.accept(new CompileException(ex.position, CompileExceptionCode.PARSE_ERROR, ex.message));
			}
		}
		for (SourcePackage subpkg : directory.getSubPackages()) {
			CompilingPackage innerPackage = pkg.getOrCreatePackage(subpkg.getName());
			pkg.addPackage(subpkg.getName(), innerPackage);
			parse(files, innerPackage, bracketParser, subpkg, exceptionLogger);
		}
	}
}
