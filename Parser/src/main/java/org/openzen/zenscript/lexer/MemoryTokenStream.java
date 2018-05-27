/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

/**
 *
 * @author Hoofdgebruiker
 */
public interface MemoryTokenStream<TT extends TokenType, T extends Token<TT>>
		extends TokenStream<TT, T> {
	void pushMark();
	
	void popMark();
	
	void reset();
}
