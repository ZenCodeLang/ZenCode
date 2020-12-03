package org.openzen.zenscript.parser;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import static org.openzen.zenscript.lexer.ZSTokenType.*;

public class ParsedImport {
	public static ParsedImport parse(CodePosition position, ZSTokenParser tokens) throws ParseException {
		try {
			boolean relative = tokens.optional(T_DOT) != null;

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
			return new ParsedImport(position, relative, importName, rename);
		} catch (ParseException ex) {
			tokens.recoverUntilTokenOrNewline(T_SEMICOLON);
			throw ex;
		}
	}
	
	public final CodePosition position;
	private final boolean relative;
	private final List<String> importName;
	private final String rename;
	
	public ParsedImport(CodePosition position, boolean relative, List<String> importName, String rename) {
		this.position = position;
		this.relative = relative;
		this.importName = importName;
		this.rename = rename;
	}
	
	public String getName() {
		return rename == null ? importName.get(importName.size() - 1) : rename;
	}
	
	public boolean isRelative() {
		return relative;
	}
	
	public List<String> getPath() {
		return importName;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < importName.size(); i++) {
			if (i > 0)
				result.append('.');
			result.append(importName.get(i));
		}
		return result.toString();
	}
}
