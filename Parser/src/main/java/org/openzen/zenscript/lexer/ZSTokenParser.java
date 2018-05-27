/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import java.io.IOException;
import java.io.Reader;
import org.openzen.zenscript.codemodel.WhitespaceInfo;

/**
 *
 * @author Hoofdgebruiker
 */
public class ZSTokenParser extends LLParserTokenStream<ZSTokenType, ZSToken> {
	public static TokenParser<ZSToken, ZSTokenType> createRaw(String filename, CharReader reader) {
		return new TokenParser<>(
				filename,
				reader,
				DFA,
				ZSTokenType.EOF,
				ZSTokenType.INVALID,
				ZSTokenFactory.INSTANCE);
	}
	
	public static ZSTokenParser create(String filename, Reader reader) throws IOException {
		return create(filename, createRaw(filename, new ReaderCharReader(reader)));
	}
	
	public static ZSTokenParser create(String filename, TokenStream<ZSTokenType, ZSToken> tokens) {
		return new ZSTokenParser(filename, new WhitespaceFilteringParser(tokens));
	}
	
	private static final CompiledDFA DFA = CompiledDFA.createLexerDFA(ZSTokenType.values(), ZSTokenType.class);
	
	private final String filename;
	private final WhitespaceFilteringParser whitespaceFilteringParser;
	
	private ZSTokenParser(String filename, WhitespaceFilteringParser<ZSTokenType, ZSToken> parser) {
		super(parser);
		
		this.whitespaceFilteringParser = parser;
		this.filename = filename;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getLastWhitespace() {
		return whitespaceFilteringParser.getLastWhitespace();
	}
	
	public void skipWhitespaceNewline() {
		whitespaceFilteringParser.skipWhitespaceNewline();
	}
	
	public WhitespaceInfo collectWhitespaceInfo(String whitespace, boolean skipLineBefore) {
		return WhitespaceInfo.from(whitespace, whitespaceFilteringParser.grabWhitespaceLine(), skipLineBefore);
	}
}
