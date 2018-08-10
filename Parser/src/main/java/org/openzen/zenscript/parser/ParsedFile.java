/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.WhitespacePostComment;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.FileResolutionContext;
import org.openzen.zenscript.codemodel.context.ModuleTypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.lexer.ZSTokenParser;
import static org.openzen.zenscript.lexer.ZSTokenType.*;
import org.openzen.zenscript.codemodel.scope.FileScope;
import org.openzen.zenscript.codemodel.scope.GlobalScriptScope;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.parser.statements.ParsedStatement;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedFile {
	public static ParsedFile parse(ZSPackage pkg, CompilingPackage compilingPackage, BracketExpressionParser bracketParser, File file) throws IOException {
		return parse(pkg, compilingPackage, bracketParser, new FileSourceFile(file.getName(), file));
	}
	
	public static ParsedFile parse(ZSPackage pkg, CompilingPackage compilingPackage, BracketExpressionParser bracketParser, String filename, String content) {
		try {
			return parse(pkg, compilingPackage, bracketParser, new LiteralSourceFile(filename, content));
		} catch (IOException ex) {
			throw new AssertionError(); // shouldn't happen
		}
	}
	
	public static ParsedFile parse(ZSPackage pkg, CompilingPackage compilingPackage, BracketExpressionParser bracketParser, SourceFile file) throws IOException {
		ZSTokenParser tokens = ZSTokenParser.create(file, bracketParser, 4);
		return parse(pkg, compilingPackage, tokens);
	}
	
	public static ParsedFile parse(ZSPackage pkg, CompilingPackage compilingPackage, ZSTokenParser tokens) {
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
					case K_EXPORT:
						modifiers |= Modifiers.EXPORT;
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
				ParsedDefinition definition = ParsedDefinition.parse(pkg, position, modifiers, annotations, tokens, null);
				if (definition == null) {
					result.statements.add(ParsedStatement.parse(tokens, annotations));
				} else {
					result.definitions.add(definition);
					definition.getCompiled().setTag(SourceFile.class, tokens.getFile());
					
					if (definition.getName() != null)
						compilingPackage.addType(definition.getName(), definition);
				}
			}
		}
		
		result.postComment = WhitespacePostComment.fromWhitespace(tokens.getLastWhitespace());
		return result;
	}
	
	public final SourceFile file;
	
	private final List<ParsedImport> imports = new ArrayList<>();
	private final List<ParsedDefinition> definitions = new ArrayList<>();
	private final List<ParsedStatement> statements = new ArrayList<>();
	private WhitespacePostComment postComment = null;
	
	public ParsedFile(SourceFile file) {
		this.file = file;
	}
	
	public boolean hasErrors() {
		return false;
	}
	
	public void printErrors() {
		
	}
	
	public void listDefinitions(PackageDefinitions definitions) {
		for (ParsedDefinition definition : this.definitions) {
			definitions.add(definition.getCompiled());
		}
	}
	
	public void compileTypes(
			ModuleTypeResolutionContext moduleContext,
			ZSPackage rootPackage,
			CompilingPackage modulePackage) {
		FileResolutionContext context = new FileResolutionContext(moduleContext);
		loadImports(context, rootPackage, modulePackage);
		for (ParsedDefinition definition : this.definitions) {
			definition.linkTypes(context);
		}
	}
	
	public void registerMembers(
			ModuleTypeResolutionContext moduleContext,
			PrecompilationState precompiler,
			ZSPackage rootPackage,
			CompilingPackage modulePackage,
			List<ExpansionDefinition> expansions,
			Map<String, ISymbol> globals) {
		FileResolutionContext context = new FileResolutionContext(moduleContext);
		loadImports(context, rootPackage, modulePackage);
		
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
			Map<String, ISymbol> globals) {
		FileResolutionContext context = new FileResolutionContext(moduleContext);
		loadImports(context, rootPackage, modulePackage);
		
		FileScope scope = new FileScope(context, expansions, globals, precompiler);
		for (ParsedDefinition definition : this.definitions) {
			definition.compile(scope);
		}
		
		if (!statements.isEmpty() || postComment != null) {
			StatementScope statementScope = new GlobalScriptScope(scope);
			List<Statement> statements = new ArrayList<>();
			for (ParsedStatement statement : this.statements) {
				statements.add(statement.compile(statementScope));
			}
			
			ScriptBlock block = new ScriptBlock(statements);
			block.setTag(SourceFile.class, file);
			block.setTag(WhitespacePostComment.class, postComment);
			scripts.add(block);
		}
	}
	
	private void loadImports(FileResolutionContext context, ZSPackage rootPackage, CompilingPackage modulePackage) {
		for (ParsedImport importEntry : imports) {
			HighLevelDefinition definition;
			if (importEntry.isRelative()) {
				definition = modulePackage.getImport(context, importEntry.getPath());
			} else {
				definition = rootPackage.getImport(importEntry.getPath(), 0);
			}
			
			if (definition == null)
				throw new CompileException(importEntry.position, CompileExceptionCode.IMPORT_NOT_FOUND, "Could not find type " + importEntry.toString());
			
			context.addImport(importEntry.getName(), definition);
		}
	}
}
