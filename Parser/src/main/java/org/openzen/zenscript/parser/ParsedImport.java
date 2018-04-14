/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenStream;
import static org.openzen.zenscript.lexer.ZSTokenType.*;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedImport {
	public static ParsedImport parse(CodePosition position, ZSTokenStream tokens) {
		List<String> importName = new ArrayList<>();
		ZSToken tName = tokens.required(T_IDENTIFIER, "identifier expected");
		importName.add(tName.content);

		while (tokens.optional(T_DOT) != null) {
			ZSToken tNamePart = tokens.required(T_IDENTIFIER, "identifier expected");
			importName.add(tNamePart.content);
		}

		String rename = null;
		if (tokens.optional(K_AS) != null) {
			ZSToken tRename = tokens.required(T_IDENTIFIER, "identifier expected");
			rename = tRename.content;
		}

		tokens.required(T_SEMICOLON, "; expected");
		return new ParsedImport(position, importName, rename);
	}
	
	public final CodePosition position;
	private final List<String> importName;
	private final String rename;
	
	public ParsedImport(CodePosition position, List<String> importName, String rename) {
		this.position = position;
		this.importName = importName;
		this.rename = rename;
	}
	
	public String getName() {
		return rename == null ? importName.get(importName.size() - 1) : rename;
	}
	
	public List<String> getPath() {
		return importName;
	}
}
