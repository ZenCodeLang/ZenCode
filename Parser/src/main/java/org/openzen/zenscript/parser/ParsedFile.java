/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.AccessScope;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import org.openzen.zenscript.lexer.ZSTokenStream;
import static org.openzen.zenscript.lexer.ZSTokenType.*;
import org.openzen.zenscript.linker.FileScope;
import org.openzen.zenscript.linker.GlobalScriptScope;
import org.openzen.zenscript.linker.symbol.ISymbol;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.statements.ParsedStatement;
import org.openzen.zenscript.shared.SourceFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedFile {
	public static ParsedFile parse(File file) throws IOException {
		String filename = file.toString();
		try (FileReader reader = new FileReader(file)) {
			return parse(filename, reader);
		}
	}
	
	public static ParsedFile parse(String filename, String content) {
		try (StringReader reader = new StringReader(content)) {
			return parse(filename, reader);
		} catch (IOException ex) {
			throw new AssertionError(); // supposed to never happen in a StringReader
		}
	}
	
	public static ParsedFile parse(String filename, Reader reader) throws IOException {
		ParsedFile result = new ParsedFile(filename);
		
		ZSTokenStream tokens = new ZSTokenStream(filename, reader);

		while (true) {
			CodePosition position = tokens.peek().position;
			int modifiers = 0;
			while (true) {
				if (tokens.optional(K_PUBLIC) != null) {
					modifiers |= Modifiers.MODIFIER_PUBLIC;
				} else if (tokens.optional(K_PRIVATE) != null) {
					modifiers |= Modifiers.MODIFIER_PRIVATE;
				} else if (tokens.optional(K_EXPORT) != null) {
					modifiers |= Modifiers.MODIFIER_EXPORT;
				} else if (tokens.optional(K_ABSTRACT) != null) {
					modifiers |= Modifiers.MODIFIER_ABSTRACT;
				} else if (tokens.optional(K_FINAL) != null) {
					modifiers |= Modifiers.MODIFIER_FINAL;
				} else if (tokens.optional(K_PROTECTED) != null) {
					modifiers |= Modifiers.MODIFIER_PROTECTED;
				} else if (tokens.optional(K_IMPLICIT) != null) {
					modifiers |= Modifiers.MODIFIER_IMPLICIT;
				} else {
					break;
				}
			}

			if (tokens.optional(K_IMPORT) != null) {
				result.imports.add(ParsedImport.parse(position, tokens));
			} else if (tokens.optional(EOF) != null) {
				break;
			} else {
				ParsedDefinition definition = ParsedDefinition.parse(position, modifiers, tokens, null);
				if (definition == null) {
					result.statements.add(ParsedStatement.parse(tokens));
				} else {
					result.definitions.add(definition);
				}

				//tokens.required(EOF, "An import, class, interface, enum, struct, function or alias expected.");
			}
		}
		
		return result;
	}
	
	public final String filename;
	
	private final List<ParsedImport> imports = new ArrayList<>();
	private final List<ParsedDefinition> definitions = new ArrayList<>();
	private final List<ParsedStatement> statements = new ArrayList<>();
	private final AccessScope access = new AccessScope();
	
	public ParsedFile(String filename) {
		this.filename = filename;
	}
	
	public boolean hasErrors() {
		return false;
	}
	
	public void printErrors() {
		
	}
	
	public void listDefinitions(PackageDefinitions definitions) {
		for (ParsedDefinition definition : this.definitions) {
			definitions.add(definition.getCompiled());
			definition.linkInnerTypes();
		}
	}
	
	public void compileMembers(
			ZSPackage rootPackage,
			PackageDefinitions packageDefinitions,
			GlobalTypeRegistry globalRegistry,
			List<ExpansionDefinition> expansions,
			Map<String, ISymbol> globalSymbols) {
		FileScope scope = new FileScope(access, rootPackage, packageDefinitions, globalRegistry, expansions, globalSymbols);
		loadImports(scope, rootPackage);
		for (ParsedDefinition definition : this.definitions) {
			definition.compileMembers(scope);
		}
	}
	
	public void compileCode(
			ZSPackage rootPackage,
			PackageDefinitions packageDefinitions,
			GlobalTypeRegistry globalRegistry,
			List<ExpansionDefinition> expansions,
			List<ScriptBlock> scripts,
			Map<String, ISymbol> globalSymbols) {
		FileScope scope = new FileScope(access, rootPackage, packageDefinitions, globalRegistry, expansions, globalSymbols);
		loadImports(scope, rootPackage);
		for (ParsedDefinition definition : this.definitions) {
			definition.compileCode(scope);
		}
		
		if (!statements.isEmpty()) {
			StatementScope statementScope = new GlobalScriptScope(scope);
			List<Statement> statements = new ArrayList<>();
			for (ParsedStatement statement : this.statements) {
				statements.add(statement.compile(statementScope));
			}
			
			ScriptBlock block = new ScriptBlock(statements);
			block.setTag(SourceFile.class, new SourceFile(filename));
			scripts.add(block);
		}
	}
	
	private void loadImports(FileScope scope, ZSPackage rootPackage) {
		for (ParsedImport importEntry : imports) {
			HighLevelDefinition definition = rootPackage.getImport(importEntry.getPath(), 0);
			if (definition == null)
				throw new CompileException(importEntry.position, CompileExceptionCode.IMPORT_NOT_FOUND, "Could not find type");
			
			scope.register(importEntry.getName(), definition);
		}
	}
}
