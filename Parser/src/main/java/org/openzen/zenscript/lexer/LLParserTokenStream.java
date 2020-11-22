/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import java.util.LinkedList;
import java.util.Stack;
import org.openzen.zencode.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class LLParserTokenStream<TT extends TokenType, T extends Token<TT>> extends WhitespaceFilteringParser<TT, T> {
    private final LinkedList<PositionedToken> tokenMemory = new LinkedList<>();
    private final Stack<Integer> marks = new Stack<>();
	
    private int tokenMemoryOffset = 0;
    private int tokenMemoryCurrent = 0;
	
	public LLParserTokenStream(TokenStream<TT, T> stream) throws ParseException {
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
	public T next() throws ParseException {
		if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
            return tokenMemory.get((tokenMemoryCurrent++) - tokenMemoryOffset).token;
        } else {
            T result = super.next();
            if (marks.isEmpty()) {
                tokenMemoryOffset++;
            } else {
                tokenMemory.add(new PositionedToken(getPosition(), getPositionBeforeWhitespace(), result));
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
	
	@Override
	public CodePosition getPositionBeforeWhitespace() {
		if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
            return tokenMemory.get((tokenMemoryCurrent) - tokenMemoryOffset).positionBeforeWhitespace;
        } else {
            return super.getPositionBeforeWhitespace();
        }
	}
	
	public boolean isNext(TT type) {
		return peek().getType() == type;
	}
	
	public T optional(TT type) throws ParseException {
		if (peek().getType() == type) {
            return next();
        } else {
            return null;
        }
	}
	
	public T required(TT type, String error) throws ParseException {
		T t = peek();
        if (t.getType() == type) {
            return next();
        } else {
			throw new ParseException(getPosition().withLength(t.getContent().length()), error);
        }
    }
	
	public boolean hasNext() {
		return peek().getType() != getEOF();
	}
	
	public void recoverUntilTokenOrNewline(ZSTokenType type) throws ParseException {
		CodePosition last = getPosition();
		while (peek().getType() != type && getPosition().fromLine > last.fromLine)
			next();
	}

    /**
     * Moves further until peek type is provided type
     */
	public void recoverUntilBeforeToken(ZSTokenType type) throws ParseException {
		while (peek().getType() != type && peek().getType() != getEOF())
			next();
	}

    /**
     * Moves further until pointer is standing on the given type
     */
	public void recoverUntilOnToken(ZSTokenType type) throws ParseException {
	    recoverUntilBeforeToken(type);
	    next();
    }
	
	private class PositionedToken {
		public final CodePosition position;
		public final CodePosition positionBeforeWhitespace;
		public final T token;
		
		public PositionedToken(CodePosition position, CodePosition positionBeforeWhitespace, T token) {
			this.position = position;
			this.positionBeforeWhitespace = positionBeforeWhitespace;
			this.token = token;
		}
	}
}
