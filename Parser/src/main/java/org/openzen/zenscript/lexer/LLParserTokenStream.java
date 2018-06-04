/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import java.util.LinkedList;
import java.util.Stack;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class LLParserTokenStream<TT extends TokenType, T extends Token<TT>> extends WhitespaceFilteringParser<TT, T> {
    private final LinkedList<PositionedToken> tokenMemory = new LinkedList<>();
    private final Stack<Integer> marks = new Stack<>();
	
    private int tokenMemoryOffset = 0;
    private int tokenMemoryCurrent = 0;
	
	public LLParserTokenStream(TokenStream<TT, T> stream) {
		super(stream);
	}
	
	public void pushMark() {
        marks.push(tokenMemoryCurrent);
	}
	
	public void popMark() {
        marks.pop();

        if (marks.isEmpty()) {
            tokenMemoryOffset = tokenMemoryCurrent;
            tokenMemory.clear();
        }
	}
	
	public void reset() {
        tokenMemoryCurrent = marks.pop();
	}

	@Override
	public T peek() {
		if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
            return tokenMemory.get((tokenMemoryCurrent) - tokenMemoryOffset).token;
        } else {
            return super.peek();
        }
	}
	
	@Override
	public T next() {
		if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
            return tokenMemory.get((tokenMemoryCurrent++) - tokenMemoryOffset).token;
        } else {
            T result = super.next();
            if (marks.isEmpty()) {
                tokenMemoryOffset++;
            } else {
                tokenMemory.add(new PositionedToken(getPosition(), result));
            }
            tokenMemoryCurrent++;
			return result;
        }
	}
	
	@Override
	public CodePosition getPosition() {
		if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
            return tokenMemory.get((tokenMemoryCurrent) - tokenMemoryOffset).position;
        } else {
            return super.getPosition();
        }
	}
	
	public boolean isNext(TT type) {
		return peek().getType() == type;
	}
	
	public T optional(TT type) {
		if (peek().getType() == type) {
            return next();
        } else {
            return null;
        }
	}
	
	public T required(TT type, String error) throws CompileException {
		T t = peek();
        if (t.getType() == type) {
            return next();
        } else {
			throw new CompileException(getPosition(), CompileExceptionCode.UNEXPECTED_TOKEN, error);
        }
    }
	
	public boolean hasNext() {
		return peek().getType() != getEOF();
	}
	
	private class PositionedToken {
		public final CodePosition position;
		public final T token;
		
		public PositionedToken(CodePosition position, T token) {
			this.position = position;
			this.token = token;
		}
	}
}
