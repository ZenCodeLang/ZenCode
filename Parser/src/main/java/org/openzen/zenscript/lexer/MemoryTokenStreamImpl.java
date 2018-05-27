/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import java.util.LinkedList;
import java.util.Stack;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class MemoryTokenStreamImpl<TT extends TokenType, T extends Token<TT>> implements TokenStream<TT, T> {
	private final TokenStream<TT, T> stream;
    private final LinkedList<TokenParser.PositionedToken<T>> tokenMemory = new LinkedList<>();
    private final Stack<Integer> marks = new Stack<>();
	
    private int tokenMemoryOffset = 0;
    private int tokenMemoryCurrent = 0;
	
	public MemoryTokenStreamImpl(TokenStream<TT, T> stream) {
		this.stream = stream;
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
            return stream.peek();
        }
	}

	@Override
	public void replace(TT other) {
		stream.replace(other);
	}

	@Override
	public T next() {
		if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
            return tokenMemory.get((tokenMemoryCurrent++) - tokenMemoryOffset).token;
        } else {
            T result = stream.peek();
            if (marks.isEmpty()) {
                tokenMemoryOffset++;
            } else {
                tokenMemory.add(new TokenParser.PositionedToken<>(stream.getPosition(), stream.peek()));
            }
            tokenMemoryCurrent++;
            stream.next();
			return result;
        }
	}

	@Override
	public CodePosition getPosition() {
		if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
            return tokenMemory.get((tokenMemoryCurrent) - tokenMemoryOffset).position;
        } else {
            return stream.getPosition();
        }
	}
}
