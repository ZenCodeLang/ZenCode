/* Licensed under GPLv3 - https://opensource.org/licenses/GPL-3.0 */
package org.openzen.zenscript.lexer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

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
public class TokenParser<T extends Token<TT>, TT extends TokenType> implements TokenStream<TT, T>
{
	private final String filename;
    private final CountingReader reader;
    private final CompiledDFA<TT> dfa;
	private final TT eof;
	private final TT invalid;
	private final int tabSize = 4;
	private final TokenFactory<T, TT> factory;
	
    private PositionedToken<T> next;
	
    private int nextChar;
    private int line;
    private int lineOffset;
	
    /**
     * Creates a token stream using the specified reader and DFA.
     *
	 * @param filename filename
     * @param reader reader to read characters from
     * @param dfa DFA to tokenize the stream
	 * @param eof end of file token type
     */
    public TokenParser(
			String filename,
			Reader reader, 
			CompiledDFA<TT> dfa,
			TT eof,
			TT invalid,
			TokenFactory<T, TT> factory)
	{
		if (eof.isWhitespace()) // important for the advance() method
			throw new IllegalArgumentException("EOF cannot be whitespace");
		
        this.reader = new CountingReader(reader);
        this.dfa = dfa;
		this.filename = filename;
		this.eof = eof;
		this.invalid = invalid;
		this.factory = factory;
		
		try {
	        nextChar = this.reader.read();
		} catch (IOException ex) {
			throw new CompileException(getPosition(), CompileExceptionCode.INTERNAL_ERROR, ex.getMessage());
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
    public TokenParser(String filename, String data, CompiledDFA<TT> dfa, TT eof, TT invalid, TokenFactory<T, TT> factory)
	{
        this(filename, new StringReader(data), dfa, eof, invalid, factory);
    }
	
	@Override
    public T peek()
	{
        return next.token;
    }
	
	@Override
	public CodePosition getPosition()
	{
		return next.position;
	}
	
	/**
	 * Replaces the current token with another one. Used to split composite tokens.
	 * 
	 * @param other 
	 */
	public void replace(TT other) {
		next = new PositionedToken(
				next.position,
				factory.create(
						other,
						next.token.getContent()));
	}

	@Override
    public T next()
	{
        T result = next.token;
		advance();
		return result;
    }
	
	@Override
	public TT getEOF() {
		return eof;
	}
	
    // =======================
    // === Private methods ===
    // =======================
	
    private void advance()
	{
        if (nextChar < 0) {
			CodePosition position = new CodePosition(
					filename,
					line,
					lineOffset,
					line,
					lineOffset);
			
            next = new PositionedToken(position, factory.create(eof, ""));
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
			
			CodePosition position = new CodePosition(filename, fromLine, fromLineOffset, line, lineOffset);
            if (dfa.finals[state] != null) {
                if (state == 0) {
					value.append((char) nextChar);
					next = new PositionedToken(position, factory.create(invalid, value.toString()));
					nextChar = reader.read();
				}
				
				next = new PositionedToken(position, factory.create(dfa.finals[state], value.toString()));
            } else {
				if (nextChar < 0 && value.length() == 0)
					return; // happens on comments at the end of files
				
				value.append((char) nextChar);
				next = new PositionedToken(position, factory.create(invalid, value.toString()));
				nextChar = reader.read();
            }
        } catch (IOException ex) {
			throw new CompileException(getPosition(), CompileExceptionCode.INTERNAL_ERROR, ex.getMessage());
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
	
	public static class PositionedToken<T> {
		public final CodePosition position;
		public final T token;
		
		public PositionedToken(CodePosition position, T token) {
			this.position = position;
			this.token = token;
		}
	}
}
