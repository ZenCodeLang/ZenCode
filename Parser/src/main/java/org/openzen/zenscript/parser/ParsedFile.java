package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.*;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.logger.ParserLogger;
import org.openzen.zenscript.parser.statements.ParsedStatement;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.openzen.zenscript.lexer.ZSTokenType.EOF;
import static org.openzen.zenscript.lexer.ZSTokenType.K_IMPORT;

public class ParsedFile {
	public final SourceFile file;
	private final List<ParsedImport> imports = new ArrayList<>();
	private final List<ParsedDefinition> definitions = new ArrayList<>();
	private final List<ParsedStatement> statements = new ArrayList<>();
	private final List<ParseException> errors = new ArrayList<>();
	private WhitespacePostComment postComment = null;

	public ParsedFile(SourceFile file) {
		this.file = file;
	}

	public static SemanticModule compileSyntaxToSemantic(
			SemanticModule[] dependencies,
			CompilingPackage pkg,
			ParsedFile[] files,
			ModuleSpace registry,
			FunctionParameter[] parameters,
			ParserLogger logger) {
		boolean failed = false;
		for (ParsedFile file : files) {
			if (file.hasErrors()) {
				failed = true;
				for (ParseException error : file.errors) {
					logger.logParseException(error);
				}
			}
		}

		// We are considering all these files to be in the same package, so make
		// a single PackageDefinition instance. If these files were in multiple
		// packages, we'd need an instance for every package.
		List<CompilingDefinition> definitions = new ArrayList<>();
		List<CompilingExpansion> expansions = new ArrayList<>();
		Map<ParsedFile, ParsedFileCompiler> definitionCompilers = new HashMap<>();

		CompileContext context = new CompileContext(
				registry.rootPackage,
				pkg.getPackage(),
				registry.collectExpansions(),
				registry.collectGlobals(),
				registry.getAnnotations());

		for (ParsedFile file : files) {
			// listDefinitions will merely register all definitions (classes,
			// interfaces, functions ...) so they can later be available to
			// the other files as well. It doesn't yet compile anything.
			ParsedFileCompiler fileCompiler = new ParsedFileCompiler(context);
			definitionCompilers.put(file, fileCompiler);

			for (ParsedDefinition definition : file.definitions) {
				definition.registerCompiling(definitions, expansions, pkg, fileCompiler, null);
			}
		}


		ZSPackage rootPackage = registry.collectPackages();

		definitions = sortTopologically(definitions);

		for (ParsedFile file : files) {
			ParsedFileCompiler fileCompiler = definitionCompilers.get(file);
			for (ParsedImport import_ : file.imports) {
				if (import_.isRelative()) {
					TypeSymbol type = pkg.getPackage().getImport(import_.getPath(), 0);
					fileCompiler.addImport(import_.getName(), type);
				} else {
					TypeSymbol type = registry.rootPackage.getImport(import_.getPath(), 0);
					fileCompiler.addImport(import_.getName(), type);
				}
			}
		}

		for (CompilingExpansion expansion : expansions) {
			context.addExpansion(expansion.getCompiling());
		}
		for (CompilingDefinition definition : definitions) {
			if (!definition.isInner()) {
				definition.linkTypes();
			}
		}
		{
			List<CompileException> errors = new ArrayList<>();
			for (CompilingDefinition definition : definitions) {
				definition.prepareMembers(errors);
			}
			for (CompilingExpansion expansion : expansions) {
				expansion.prepareMembers(errors);
			}
		}

		PackageDefinitions packageDefinitions = new PackageDefinitions();
		for (CompilingDefinition definition : definitions) {
			packageDefinitions.add(definition.getDefinition());
		}

		if (failed) {
			return new SemanticModule(
					pkg.module,
					dependencies,
					parameters,
					SemanticModule.State.INVALID,
					rootPackage,
					pkg.getPackage(),
					new PackageDefinitions(),
					Collections.emptyList(),
					expansions.stream().map(CompilingExpansion::getCompiling).collect(Collectors.toList()),
					registry.getAnnotations().toArray(new AnnotationDefinition[0]),
					logger
			);
		}
		List<CompileException> errors = new ArrayList<>();
		for (CompilingDefinition definition : definitions) {
			definition.compileMembers(errors);
		}
		for (CompilingExpansion expansion : expansions) {
			expansion.compileMembers(errors);
		}

		List<ScriptBlock> scripts = new ArrayList<>();
		FunctionHeader scriptHeader = new FunctionHeader(BasicTypeID.VOID, parameters);
		for (ParsedFile file : files) {
			if (!file.statements.isEmpty() || file.postComment != null) {
				StatementCompiler compiler = definitionCompilers.get(file).forScripts(scriptHeader);
				List<Statement> statements = file.statements.stream()
					.map(statement -> statement.compile(compiler))
					.collect(Collectors.toList());
				ScriptBlock block = new ScriptBlock(file.file, pkg.module, pkg.getPackage(), scriptHeader, statements);
				block.setTag(WhitespacePostComment.class, file.postComment);
				scripts.add(block);
			}
		}

		for (CompileException error : errors) {
			logger.logCompileException(error);
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
				expansions,
				registry.getAnnotations(),
				logger);
	}

	private static List<CompilingDefinition> sortTopologically(List<CompilingDefinition> definitions) {
		List<CompilingDefinition> result = new ArrayList<>();
		Set<TypeSymbol> visited = new HashSet<>();
		Map<TypeSymbol, CompilingDefinition> definitionsByType = new HashMap<>();

		for (CompilingDefinition definition : definitions) {
			definitionsByType.put(definition.getDefinition(), definition);
		}

		for (CompilingDefinition definition : definitions) {
			sortTopologically(result, definition, visited, definitionsByType);
		}

		return result;
	}

	private static void sortTopologically(
			List<CompilingDefinition> result,
			CompilingDefinition definition,
			Set<TypeSymbol> visited,
			Map<TypeSymbol, CompilingDefinition> definitionsByType
	) {
		if (visited.contains(definition.getDefinition()))
			return;

		visited.add(definition.getDefinition());
		for (TypeSymbol type : definition.getDependencies()) {
			if (definitionsByType.containsKey(type)) {
				sortTopologically(result, definitionsByType.get(type), visited, definitionsByType);
			}
		}
		result.add(definition);
	}

	public static ParsedFile parse(BracketExpressionParser bracketParser, File file) throws ParseException {
		return parse(bracketParser, new FileSourceFile(file.getName(), file));
	}

	public static ParsedFile parse(BracketExpressionParser bracketParser, String filename, String content) throws ParseException {
		return parse( bracketParser, new LiteralSourceFile(filename, content));
	}

	public static ParsedFile parse(BracketExpressionParser bracketParser, SourceFile file) throws ParseException {
		try {
			ZSTokenParser tokens = ZSTokenParser.create(file, bracketParser);
			return parse(tokens);
		} catch (IOException ex) {
			throw new ParseException(new CodePosition(file, 0, 0, 0, 0), ex.getMessage());
		}
	}

	public static ParsedFile parse(ZSTokenParser tokens) throws ParseException {
		ParsedFile result = new ParsedFile(tokens.getFile());

		while (true) {
			CodePosition position = tokens.getPosition();
			ParsedAnnotation[] annotations = ParsedAnnotation.parseAnnotations(tokens);
			int modifiers = 0;
			outer:
			while (true) {
				switch (tokens.peek().type) {
					case K_PUBLIC:
						modifiers |= Modifiers.FLAG_PUBLIC;
						break;
					case K_PRIVATE:
						modifiers |= Modifiers.FLAG_PRIVATE;
						break;
					case K_INTERNAL:
						modifiers |= Modifiers.FLAG_INTERNAL;
						break;
					case K_EXTERN:
						modifiers |= Modifiers.FLAG_EXTERN;
						break;
					case K_ABSTRACT:
						modifiers |= Modifiers.FLAG_ABSTRACT;
						break;
					case K_FINAL:
						modifiers |= Modifiers.FLAG_FINAL;
						break;
					case K_PROTECTED:
						modifiers |= Modifiers.FLAG_PROTECTED;
						break;
					case K_IMPLICIT:
						modifiers |= Modifiers.FLAG_IMPLICIT;
						break;
					case K_VIRTUAL:
						modifiers |= Modifiers.FLAG_VIRTUAL;
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
				ParsedDefinition definition = ParsedDefinition.parse(position, modifiers, annotations, tokens);
				if (definition == null) {
					try {
						result.statements.add(ParsedStatement.parse(tokens, annotations));
					} catch (ParseException e) {
						tokens.logError(e);
						tokens.recoverUntilOnToken(ZSTokenType.T_SEMICOLON);
					}
				} else {
					result.definitions.add(definition);
				}
			}
		}

		result.postComment = WhitespacePostComment.fromWhitespace(tokens.getLastWhitespace());
		result.errors.addAll(tokens.getErrors());
		return result;
	}

	public boolean hasErrors() {
		return errors.size() > 0;
	}

	public List<ParseException> getErrors() {
		return errors;
	}
}
