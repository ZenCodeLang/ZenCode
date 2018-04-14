/* Licensed under GPLv3 - https://opensource.org/licenses/GPL-3.0 */
package org.openzen.zenscript.lexer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import org.openzen.zenscript.shared.CodePosition;

/**
 * Represents a token stream. A token stream reads characters from a reader and
 * presents it as a series of tokens. Can be used to implement LL(*) parsers.
 *
 * Token classes with a negative class are considered to be whitespace.
 *
 * @author Stan Hebben
 * @param <T> token class
 * @param <TT> token type class
 */
public abstract class TokenStream<T extends Token<TT>, TT extends TokenType> implements Iterator<T>
{
	private final String filename;
    private final CountingReader reader;
    private final CompiledDFA<TT> dfa;
    private final LinkedList<T> tokenMemory;
    private final Stack<Integer> marks;
	private final TT eof;
	private final int tabSize = 4;
	
    private T next;
    private int nextChar;
    private int line;
    private int lineOffset;
	
    private int tokenMemoryOffset;
    private int tokenMemoryCurrent;
    
    /**
     * Creates a token stream using the specified reader and DFA.
     *
	 * @param filename filename
     * @param reader reader to read characters from
     * @param dfa DFA to tokenize the stream
	 * @param eof end of file token type
     */
    public TokenStream(String filename, Reader reader, CompiledDFA<TT> dfa, TT eof)
	{
		if (eof.isWhitespace()) // important for the advance() method
			throw new IllegalArgumentException("EOF cannot be whitespace");
		
        tokenMemoryOffset = 0;
        tokenMemoryCurrent = 0;
        tokenMemory = new LinkedList<>();
        marks = new Stack<>();
        
        this.reader = new CountingReader(reader);
        this.dfa = dfa;
		this.filename = filename;
		this.eof = eof;
		
		try {
	        nextChar = this.reader.read();
		} catch (IOException ex) {
			ioException(ex);
		}
		
        line = 1;
        lineOffset = 1;
        advance();
    }

    /**
     * Creates a token stream which reads data from the specified string.
     *
	 * @param filename filename
     * @param data data to read
     * @param dfa DFA to tokenize the stream
	 * @param eof end of file token type
     */
    public TokenStream(String filename, String data, CompiledDFA<TT> dfa, TT eof)
	{
        this(filename, new StringReader(data), dfa, eof);
    }
	
	public String getFile()
	{
		return filename;
	}
	
	public int getLine()
	{
		return line;
	}
	
	public int getLineOffset()
	{
		return lineOffset;
	}
    
    public T peek()
	{
        if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
            return tokenMemory.get((tokenMemoryCurrent) - tokenMemoryOffset);
        } else {
            return next;
        }
    }

    public boolean isNext(TT type)
	{
        return peek().getType() == type;
    }

    public T optional(TT type)
	{
        if (peek().getType() == type) {
            return next();
        } else {
            return null;
        }
    }

    public T required(TT type, String error)
	{
		T t = peek();
        if (t.getType() == type) {
            return next();
        } else {
			requiredTokenNotFound(getPosition(), error, t);
			return null;
        }
    }
	
	public CodePosition getPosition()
	{
		return peek().getPosition();
	}
	
	// =====================
    // === LL(*) ability ===
	// =====================

    /**
     * Pushes a mark on the mark stack.
     */
    public void pushMark()
	{
        marks.push(tokenMemoryCurrent);
    }

    /**
     * Pops a mark from the mark stack without reset.
     */
    public void popMark()
	{
        marks.pop();

        if (marks.isEmpty()) {
            tokenMemoryOffset = tokenMemoryCurrent;
            tokenMemory.clear();
        }
    }

    /**
     * Pops a mark from the mark stack and resets the stream's position to it
     */
    public void reset()
	{
        tokenMemoryCurrent = marks.pop();
    }

    // ===============================
    // === Iterator implementation ===
    // ===============================

	@Override
    public boolean hasNext()
	{
        return next.getType() != eof;
    }

	@Override
    public T next()
	{
        if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
            return tokenMemory.get((tokenMemoryCurrent++) - tokenMemoryOffset);
        } else {
            T result = next;

            if (marks.isEmpty()) {
                tokenMemoryOffset++;
            } else {
                tokenMemory.add(result);
            }
            tokenMemoryCurrent++;

            advance();
            return result;
        }
    }

	@Override
    public void remove()
	{
        throw new UnsupportedOperationException("Not supported.");
    }

    // ==================================
    // === Protected abstract methods ===
    // ==================================
	
	/**
	 * Called to create a token. May also be used to postprocess tokens while
	 * generating them.
	 * 
	 * @param position token position (range)
	 * @param value token value
	 * @param tokenType token type
	 * @return newly created token
	 */
	protected abstract T createToken(
			CodePosition position,
			String whitespaceBefore,
			String value,
			TT tokenType);
	
	/**
	 * Called when a required token could not be found. Should log an error or
	 * throw an exception. If no exception is thrown, the calling required
	 * method will return null as token value.
	 * 
	 * @param position erroring position
	 * @param error error to be logged
	 * @param token incorrect token
	 */
	protected abstract void requiredTokenNotFound(
			CodePosition position,
			String error,
			T token);
	
	/**
	 * Called when the input contains an invalid token. Should either create
	 * a token indicating an invalid token, or throw an exception.
	 * 
	 * @param position erroring position
	 * @param token token value
	 * @return a token marking an invalid token
	 */
	protected abstract T invalidToken(
			CodePosition position,
			String whitespaceBefore,
			String token);
	
	/**
	 * Called when an IO exception occurs. Should throw an exception of some
	 * kind.
	 * 
	 * @param ex exception to be logged
	 */
	protected abstract void ioException(IOException ex);
	
    // =======================
    // === Private methods ===
    // =======================

    /**
     * Advances to the next non - whitespace token.
     */
    private void advance()
	{
		StringBuilder whitespace = new StringBuilder();
        while (true) {
            advanceToken(whitespace.toString());
			if (next.getType().isWhitespace()) {
				whitespace.append(next.getContent());
			} else {
				break;
			}
        }
    }

    /**
     * Advances to the next token.
     */
    private void advanceToken(String whitespace)
	{
        if (nextChar < 0) {
			CodePosition position = new CodePosition(
					filename,
					line,
					lineOffset,
					line,
					lineOffset);
			
            next = createToken(position, "", "", eof);
            return;
        }
		
        try {
            int state = 0;
            StringBuilder value = new StringBuilder();
            int fromLine = line;
            int fromLineOffset = lineOffset;
            while (dfa.transitions[state].containsKey(nextChar)) {
                value.append((char)nextChar);
                state = dfa.transitions[state].get(nextChar);
                line = reader.line;
                lineOffset = reader.lineOffset;
                nextChar = reader.read();
            }
			
			if (line < 0)
				throw new IllegalStateException("Line cannot be negative");
			
            if (dfa.finals[state] != null) {
                if (state == 0) {
					value.append((char) nextChar);
					next = invalidToken(new CodePosition(filename, fromLine, fromLineOffset, line, lineOffset),
							whitespace,
							value.toString());
					nextChar = reader.read();
				}
				
				next = createToken(new CodePosition(filename, fromLine, fromLineOffset, line, lineOffset),
						whitespace,
						value.toString(), dfa.finals[state]);
            } else {
				if (nextChar < 0 && value.length() == 0)
					return; // happens on comments at the end of files
				
				value.append((char) nextChar);
				next = invalidToken(new CodePosition(filename, fromLine, fromLineOffset, line, lineOffset),
						whitespace,
						value.toString());
				nextChar = reader.read();
            }
        } catch (IOException ex) {
            ioException(ex);
        }
    }

    // =============================
    // === Private inner classes ===
    // =============================

    /**
     * Keeps a line and line offset count.
     */
    private class CountingReader
	{
        private int line;
        private int lineOffset;
        private Reader reader;
        private boolean eof;

        public CountingReader(Reader reader)
		{
            this.reader = reader;
            line = 1;
            lineOffset = 1;
        }

        public int read() throws IOException
		{
            int ch = reader.read();
            if (ch == -1) {
                eof = true;
                return ch;
            }
            if (ch == '\n') {
                line++;
                lineOffset = 1;
            } else if (ch == '\t') {
				lineOffset += tabSize;
			} else {
                lineOffset++;
            }
            return ch;
        }
    }
}
