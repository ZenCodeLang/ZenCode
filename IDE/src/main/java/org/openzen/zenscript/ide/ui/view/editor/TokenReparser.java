/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.lexer.CharReader;
import org.openzen.zenscript.lexer.TokenParser;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;

/**
 *
 * @author Hoofdgebruiker
 */
public class TokenReparser {
	private final List<SourceEditor.Line> lines;
	private final String filename;
	private final int toLine;
	private final int toToken;
	private final int spacesPerTab;
	
	private int lineIndex;
	private int token;
	
	public TokenReparser(String filename, List<SourceEditor.Line> lines, int fromLine, int fromToken, int toLine, int toToken, int spacesPerTab) {
		if (fromToken < 0 || toToken < 0)
			throw new IllegalArgumentException("fromToken or toToken cannot be < 0");
		
		this.lines = lines;
		this.filename = filename;
		this.toLine = toLine;
		this.toToken = toToken;
		
		this.lineIndex = fromLine;
		this.token = fromToken;
		this.spacesPerTab = spacesPerTab;
	}
	
	public List<ZSToken> reparse() {
		ReparseCharReader reader = new ReparseCharReader();
		TokenParser<ZSToken, ZSTokenType> reparser = ZSTokenParser.createRaw(filename, reader, spacesPerTab);
		List<ZSToken> result = new ArrayList<>();
		while ((lineIndex < toLine || token < toToken || !reader.isAtTokenBoundary()) && reparser.hasNext())
			result.add(reparser.next());
		return result;
	}
	
	private String get() {
		SourceEditor.Line line = lines.get(lineIndex);
		if (token == line.tokens.size())
			return "\n"; // special "newline" token for transitions to next line
		
		return line.tokens.get(token).content;
	}
	
	private boolean advance() {
		if (lineIndex == lines.size() - 1 && token == lines.get(lineIndex).tokens.size())
			return false;
		
		token++;
		if (token > lines.get(lineIndex).tokens.size()) {
			lineIndex++;
			token = 0;
		}
		return true;
	}
	
	public int getLine() {
		return lineIndex;
	}
	
	public int getToken() {
		return token;
	}
	
	private class ReparseCharReader implements CharReader {
		private String token;
		private int tokenOffset;
		
		public ReparseCharReader() {
			token = get();
		}
		
		public boolean isAtTokenBoundary() {
			return tokenOffset == 0;
		}

		@Override
		public int peek() throws IOException {
			return token == null ? -1 : token.charAt(tokenOffset);
		}

		@Override
		public int next() throws IOException {
			int result = peek();
			tokenOffset++;
			if (tokenOffset == token.length()) {
				advance();
				token = get();
				tokenOffset = 0;
			}
			return result;
		}
	}
}
