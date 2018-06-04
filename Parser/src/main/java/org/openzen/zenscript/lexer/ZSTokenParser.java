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
	private static final CompiledDFA DFA = CompiledDFA.createLexerDFA(ZSTokenType.values(), ZSTokenType.class);
	
	public static TokenParser<ZSToken, ZSTokenType> createRaw(String filename, CharReader reader, int spacesPerTab) {
		return new TokenParser<>(
				filename,
				reader,
				DFA,
				ZSTokenType.EOF,
				ZSTokenType.INVALID,
				new ZSTokenFactory(spacesPerTab));
	}
	
	public static ZSTokenParser create(String filename, Reader reader, int spacesPerTab) throws IOException {
		return new ZSTokenParser(createRaw(filename, new ReaderCharReader(reader), spacesPerTab));
	}
	
	public ZSTokenParser(TokenStream<ZSTokenType, ZSToken> parser) {
		super(parser);
	}
	
	public String getFilename() {
		return getPosition().filename;
	}
	
	public WhitespaceInfo collectWhitespaceInfo(String whitespace, boolean skipLineBefore) {
		return WhitespaceInfo.from(whitespace, grabWhitespaceLine(), skipLineBefore);
	}
}
