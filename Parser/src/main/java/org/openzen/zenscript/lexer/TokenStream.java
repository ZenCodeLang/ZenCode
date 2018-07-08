/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import org.openzen.zencode.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TokenStream<TT extends TokenType, T extends Token<TT>> {
	T next();
	
	CodePosition getPosition();
	
	TT getEOF();
}
