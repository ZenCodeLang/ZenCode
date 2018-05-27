/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import org.openzen.zenscript.codemodel.WhitespaceInfo;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ZSTokenStream extends MemoryTokenStream<ZSTokenType, ZSToken> {
	String getFilename();
	
	String getLastWhitespace();
	
	void skipWhitespaceNewline();
	
	WhitespaceInfo collectWhitespaceInfo(String whitespace, boolean skipLineBefore);
}
