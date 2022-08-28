/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import java.util.ArrayList;
import java.util.List;

import org.openzen.zencode.shared.CompileError;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.ModuleSpace;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.constructor.ConstructorException;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.constructor.module.logging.*;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.ParsedFile;

/**
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

	private static void parse(List<ParsedFile> files, CompilingPackage pkg, BracketExpressionParser bracketParser, SourcePackage directory, CompileExceptionLogger exceptionLogger) {
		for (SourceFile file : directory.getFiles()) {
			try {
				files.add(ParsedFile.parse(pkg, bracketParser, file));
			} catch (ParseException ex) {
				exceptionLogger.logCompileException(new CompileException(ex.position, new CompileError(CompileExceptionCode.PARSE_ERROR, ex.message)));
			}
		}
		for (SourcePackage subpkg : directory.getSubPackages()) {
			CompilingPackage innerPackage = pkg.getOrCreatePackage(subpkg.getName());
			pkg.addPackage(subpkg.getName(), innerPackage);
			parse(files, innerPackage, bracketParser, subpkg, exceptionLogger);
		}
	}

	@Override
	public String getName() {
		return module.getName();
	}

	@Override
	public SemanticModule load(ModuleLoader loader, ModuleLogger logger) {
		SemanticModule[] dependencies = module.loadDependencies(loader, logger);

		ModuleSpace space = new ModuleSpace(new ArrayList<>());
		for (SemanticModule module : dependencies) {
			try {
				space.addModule(module.name, module);
			} catch (CompileException ex) {
				throw new ConstructorException("Error: exception during compilation", ex);
			}
		}

		ZSPackage pkg = isStdlib ? modules.stdlib : new ZSPackage(null, getName());
		ModuleSymbol module = new ModuleSymbol(getName());
		CompilingPackage compilingPackage = new CompilingPackage(pkg, module);

		ParsedFile[] parsedFiles = parse(compilingPackage, logger);
		SemanticModule result = ParsedFile.compileSyntaxToSemantic(dependencies, compilingPackage, parsedFiles, space, FunctionParameter.NONE, logger);
		result.globals.putAll(this.module.getGlobals(result));
		return result;
	}

	@Override
	public SourcePackage getRootPackage() {
		return module.getRootPackage();
	}

	public ParsedFile[] parse(CompilingPackage compilingPackage, CompileExceptionLogger exceptionLogger) {
		// TODO: load bracket parsers from host plugins
		List<ParsedFile> files = new ArrayList<>();
		parse(files, compilingPackage, null, module.getRootPackage(), exceptionLogger);
		return files.toArray(new ParsedFile[files.size()]);
	}
}
