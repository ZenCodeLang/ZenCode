/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TokenStream<TT extends TokenType, T extends Token<TT>> {
	T peek();
	
	default boolean isNext(TT type) {
		return peek().getType() == type;
	}
	
	default T optional(TT type) {
		if (peek().getType() == type) {
            return next();
        } else {
            return null;
        }
	}
	
	default T required(TT type, String error) throws CompileException {
		T t = peek();
        if (t.getType() == type) {
            return next();
        } else {
			throw new CompileException(getPosition(), CompileExceptionCode.UNEXPECTED_TOKEN, error);
        }
    }
	
	void replace(TT other);
	
	default boolean hasNext() {
		return peek().getType() != getEOF();
	}
	
	T next();
	
	CodePosition getPosition();
	
	TT getEOF();
}
