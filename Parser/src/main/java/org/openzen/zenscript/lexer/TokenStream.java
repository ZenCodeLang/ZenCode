/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TokenStream<TT extends TokenType, T extends Token<TT>> {
	T peek();
	
	boolean isNext(TT type);
	
	T optional(TT type);
	
	T required(TT type, String error);
	
	void pushMark();
	
	void popMark();
	
	void reset();
	
	void replace(TT other);
	
	boolean hasNext();
	
	T next();
	
	CodePosition getPosition();
}
