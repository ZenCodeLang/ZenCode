/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import java.io.Reader;
import org.openzen.zenscript.codemodel.WhitespaceInfo;

/**
 *
 * @author Hoofdgebruiker
 */
public class ZSTokenParser extends MemoryTokenStreamImpl<ZSTokenType, ZSToken>
		implements ZSTokenStream {
	public static TokenParser<ZSToken, ZSTokenType> createRaw(String filename, Reader reader) {
		return new TokenParser<>(
				filename,
				reader,
				DFA,
				ZSTokenType.EOF,
				ZSTokenType.INVALID,
				ZSTokenFactory.INSTANCE);
	}
	
	public static ZSTokenParser create(String filename, Reader reader) {
		return create(filename, new WhitespaceFilteringParser(createRaw(filename, reader)));
	}
	
	public static ZSTokenParser create(String filename, WhitespaceFilteringParser<ZSTokenType, ZSToken> parser) {
		return new ZSTokenParser(filename, parser);
	}
	
	private static final CompiledDFA DFA = CompiledDFA.createLexerDFA(ZSTokenType.values(), ZSTokenType.class);
	
	private final String filename;
	private final WhitespaceFilteringParser whitespaceFilteringParser;
	
	private ZSTokenParser(String filename, WhitespaceFilteringParser<ZSTokenType, ZSToken> parser) {
		super(parser);
		
		this.whitespaceFilteringParser = parser;
		this.filename = filename;
	}
	
	@Override
	public String getFilename() {
		return filename;
	}
	
	@Override
	public String getLastWhitespace() {
		return whitespaceFilteringParser.getLastWhitespace();
	}
	
	@Override
	public void skipWhitespaceNewline() {
		whitespaceFilteringParser.skipWhitespaceNewline();
	}
	
	@Override
	public WhitespaceInfo collectWhitespaceInfo(String whitespace, boolean skipLineBefore) {
		return WhitespaceInfo.from(whitespace, whitespaceFilteringParser.grabWhitespaceLine(), skipLineBefore);
	}
	
	@Override
	public ZSTokenType getEOF() {
		return ZSTokenType.EOF;
	}
}
