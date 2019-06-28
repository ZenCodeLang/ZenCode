/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.ModuleSpace;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.WhitespacePostComment;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.FileResolutionContext;
import org.openzen.zenscript.codemodel.context.ModuleTypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.scope.FileScope;
import org.openzen.zenscript.codemodel.scope.GlobalScriptScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.parser.statements.ParsedStatement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.openzen.zenscript.lexer.ZSTokenType.EOF;
import static org.openzen.zenscript.lexer.ZSTokenType.K_IMPORT;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedFile {
	public static SemanticModule compileSyntaxToSemantic(
			SemanticModule[] dependencies,
			CompilingPackage pkg,
			ParsedFile[] files,
			ModuleSpace registry,
			FunctionParameter[] parameters,
			Consumer<CompileException> exceptionLogger) {
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
		definitions.registerExpansionsTo(expansions);
		
		Map<String, ISymbol> globals = registry.collectGlobals();
		boolean failed = false;
		
		ModuleTypeResolutionContext moduleContext = new ModuleTypeResolutionContext(
				registry.registry,
				registry.getAnnotations(),
				registry.getStorageTypes(),
				rootPackage,
				pkg,
				globals);
		
		
		//Map so we don't print multiple compile exceptions for a single import
		Map<String, CompileException> importErrors = new HashMap<>();
		for (ParsedFile file : files) {
			file.registerTypes(moduleContext, rootPackage, pkg, importErrors);
		}
		
		for (ParsedFile file : files) {
			// compileMembers will register all definition members to their
			// respective definitions, such as fields, constructors, methods...
			// It doesn't yet compile the method contents.
			file.compileTypes(moduleContext, rootPackage, pkg, importErrors);
		}
		
		if (failed)
			return new SemanticModule(pkg.module, dependencies, parameters, SemanticModule.State.INVALID, rootPackage, pkg.getPackage(), definitions, Collections.emptyList(), registry.registry, expansions, registry.getAnnotations(), registry.getStorageTypes());
		
		// scripts will store all the script blocks encountered in the files
		PrecompilationState precompiler = new PrecompilationState();
		for (ParsedFile file : files) {
			file.registerMembers(moduleContext, precompiler, rootPackage, pkg, expansions, globals, importErrors);
		}
		
		List<ScriptBlock> scripts = new ArrayList<>();
		FunctionHeader scriptHeader = new FunctionHeader(BasicTypeID.VOID, parameters);
		for (ParsedFile file : files) {
			// compileCode will convert the parsed statements and expressions
			// into semantic code. This semantic code can then be compiled
			// to various targets.
			file.compileCode(moduleContext, precompiler, rootPackage, pkg, expansions, scripts, globals, scriptHeader, exceptionLogger, importErrors);
		}
        
        for(CompileException error : importErrors.values()) {
            exceptionLogger.accept(error);
        }
		return new SemanticModule(
				pkg.module,
				dependencies,
				parameters,
				SemanticModule.State.ASSEMBLED,
				rootPackage,
				pkg.getPackage(),
				definitions,
				scripts,
				registry.registry,
				expansions,
				registry.getAnnotations(),
				registry.getStorageTypes());
	}
	
	public static ParsedFile parse(CompilingPackage compilingPackage, BracketExpressionParser bracketParser, File file) throws ParseException {
		return parse(compilingPackage, bracketParser, new FileSourceFile(file.getName(), file));
	}
	
	public static ParsedFile parse(CompilingPackage compilingPackage, BracketExpressionParser bracketParser, String filename, String content) throws ParseException {
		return parse(compilingPackage, bracketParser, new LiteralSourceFile(filename, content));
	}
	
	public static ParsedFile parse(CompilingPackage compilingPackage, BracketExpressionParser bracketParser, SourceFile file) throws ParseException {
		try {
			ZSTokenParser tokens = ZSTokenParser.create(file, bracketParser, 4);
			return parse(compilingPackage, tokens);
		} catch (IOException ex) {
			throw new ParseException(new CodePosition(file, 0, 0, 0, 0), ex.getMessage());
		}
	}
	
	public static ParsedFile parse(CompilingPackage compilingPackage, ZSTokenParser tokens) throws ParseException {
		ParsedFile result = new ParsedFile(tokens.getFile());

		while (true) {
			CodePosition position = tokens.getPosition();
			ParsedAnnotation[] annotations = ParsedAnnotation.parseAnnotations(tokens);
			int modifiers = 0;
			outer: while (true) {
				switch (tokens.peek().type) {
					case K_PUBLIC:
						modifiers |= Modifiers.PUBLIC;
						break;
					case K_PRIVATE:
						modifiers |= Modifiers.PRIVATE;
						break;
					case K_INTERNAL:
						modifiers |= Modifiers.INTERNAL;
						break;
					case K_EXTERN:
						modifiers |= Modifiers.EXTERN;
						break;
					case K_ABSTRACT:
						modifiers |= Modifiers.ABSTRACT;
						break;
					case K_FINAL:
						modifiers |= Modifiers.FINAL;
						break;
					case K_PROTECTED:
						modifiers |= Modifiers.PROTECTED;
						break;
					case K_IMPLICIT:
						modifiers |= Modifiers.IMPLICIT;
						break;
					case K_VIRTUAL:
						modifiers |= Modifiers.VIRTUAL;
						break;
					default:
						break outer;
				}
				tokens.next();
			}

			if (tokens.optional(K_IMPORT) != null) {
				result.imports.add(ParsedImport.parse(position, tokens));
			} else if ((tokens.optional(EOF)) != null) {
				break;
			} else {
				ParsedDefinition definition = ParsedDefinition.parse(compilingPackage, position, modifiers, annotations, tokens, null);
				if (definition == null) {
					result.statements.add(ParsedStatement.parse(tokens, annotations));
				} else {
					result.definitions.add(definition);
				}
			}
		}
		
		result.postComment = WhitespacePostComment.fromWhitespace(tokens.getLastWhitespace());
		result.errors.addAll(tokens.getErrors());
		return result;
	}
	
	public final SourceFile file;
	
	private final List<ParsedImport> imports = new ArrayList<>();
	private final List<ParsedDefinition> definitions = new ArrayList<>();
	private final List<ParsedStatement> statements = new ArrayList<>();
	private WhitespacePostComment postComment = null;
	private final List<ParseException> errors = new ArrayList<>();
	
	public ParsedFile(SourceFile file) {
		this.file = file;
	}
	
	public boolean hasErrors() {
		return errors.size() > 0;
	}
	
	public List<ParseException> getErrors() {
		return errors;
	}
	
	public void listDefinitions(PackageDefinitions definitions) {
		for (ParsedDefinition definition : this.definitions) {
			definitions.add(definition.getCompiled());
		}
	}
	
	public void registerTypes(
			ModuleTypeResolutionContext moduleContext,
			ZSPackage rootPackage,
			CompilingPackage modulePackage, Map<String, CompileException> importErrors) {
		FileResolutionContext context = new FileResolutionContext(moduleContext, rootPackage, modulePackage);
		loadImports(context, rootPackage, modulePackage, importErrors);
		
		for (ParsedDefinition definition : this.definitions) {
			if (definition.getName() != null)
				definition.pkg.addType(definition.getName(), definition.getCompiling(context));
		}
	}
	
	public void compileTypes(
			ModuleTypeResolutionContext moduleContext,
			ZSPackage rootPackage,
			CompilingPackage modulePackage, Map<String, CompileException> importErrors) {
		FileResolutionContext context = new FileResolutionContext(moduleContext, rootPackage, modulePackage);
		loadImports(context, rootPackage, modulePackage, importErrors);
		
		for (ParsedDefinition definition : this.definitions) {
			if (definition.getName() != null)
				modulePackage.addType(definition.getName(), definition.getCompiling(context));
		}
		
		for (ParsedDefinition definition : this.definitions) {
			definition.getCompiling(context).load();
		}
	}
	
	public void registerMembers(
			ModuleTypeResolutionContext moduleContext,
			PrecompilationState precompiler,
			ZSPackage rootPackage,
			CompilingPackage modulePackage,
			List<ExpansionDefinition> expansions,
			Map<String, ISymbol> globals, Map<String, CompileException> importErrors) {
		FileResolutionContext context = new FileResolutionContext(moduleContext, rootPackage, modulePackage);
		loadImports(context, rootPackage, modulePackage, importErrors);
		
		FileScope scope = new FileScope(context, expansions, globals, precompiler);
		for (ParsedDefinition definition : this.definitions) {
			definition.registerMembers(scope, precompiler);
		}
	}
	
	public void compileCode(
            ModuleTypeResolutionContext moduleContext,
            PrecompilationState precompiler,
            ZSPackage rootPackage,
            CompilingPackage modulePackage,
            List<ExpansionDefinition> expansions,
            List<ScriptBlock> scripts,
            Map<String, ISymbol> globals,
            FunctionHeader scriptHeader,
            Consumer<CompileException> exceptionLogger, Map<String, CompileException> importErrors) {
		FileResolutionContext context = new FileResolutionContext(moduleContext, rootPackage, modulePackage);
		loadImports(context, rootPackage, modulePackage, importErrors);
		
		FileScope scope = new FileScope(context, expansions, globals, precompiler);
		for (ParsedDefinition definition : this.definitions) {
			try {
				definition.compile(scope);
			} catch (CompileException ex) {
				exceptionLogger.accept(ex);
			}
		}
		
		if (!statements.isEmpty() || postComment != null) {
			StatementScope statementScope = new GlobalScriptScope(scope, scriptHeader);
			List<Statement> statements = new ArrayList<>();
			for (ParsedStatement statement : this.statements) {
				statements.add(statement.compile(statementScope));
			}
			
			ScriptBlock block = new ScriptBlock(file, modulePackage.module, modulePackage.getPackage(), scriptHeader, statements);
			block.setTag(WhitespacePostComment.class, postComment);
			scripts.add(block);
		}
	}
	
	private void loadImports(FileResolutionContext context, ZSPackage rootPackage, CompilingPackage modulePackage, Map<String, CompileException> importErrors) {
		for (ParsedImport importEntry : imports) {
			HighLevelDefinition definition;
			if (importEntry.isRelative()) {
				definition = modulePackage.getImport(context, importEntry.getPath());
			} else {
				definition = rootPackage.getImport(importEntry.getPath(), 0);
			}
			
			if (definition == null)
				importErrors.put(importEntry.toString(), new CompileException(importEntry.position, CompileExceptionCode.IMPORT_NOT_FOUND, "Could not find type " + importEntry.toString()));
			
			if (definition != null)
				context.addImport(importEntry.getName(), definition);
		}
	}
}
