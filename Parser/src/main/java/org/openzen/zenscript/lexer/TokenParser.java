/* Licensed under GPLv3 - https://opensource.org/licenses/GPL-3.0 */
package org.openzen.zenscript.lexer;

import java.io.IOException;
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
    private final CountingCharReader reader;
    private final CompiledDFA<TT> dfa;
	private final TT eof;
	private final TT invalid;
	private final TokenFactory<T, TT> factory;
	
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
			CharReader reader, 
			CompiledDFA<TT> dfa,
			TT eof,
			TT invalid,
			TokenFactory<T, TT> factory)
	{
		if (eof.isWhitespace()) // important for the advance() method
			throw new IllegalArgumentException("EOF cannot be whitespace");
		
        this.reader = new CountingCharReader(reader, filename, 4);
        this.dfa = dfa;
		this.eof = eof;
		this.invalid = invalid;
		this.factory = factory;
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
        this(filename, new StringCharReader(data), dfa, eof, invalid, factory);
    }
	
	@Override
	public CodePosition getPosition()
	{
		return reader.getPosition();
	}
	
	public boolean hasNext() {
		try {
			return reader.peek() >= 0;
		} catch (IOException ex) {
			return false;
		}
	}
	
	@Override
	public TT getEOF() {
		return eof;
	}
	
	@Override
    public T next()
	{
		try {
			if (reader.peek() < 0)
				return factory.create(eof, "");
			
            int state = 0;
            StringBuilder value = new StringBuilder();
            while (dfa.transitions[state].containsKey(reader.peek())) {
				char c = (char) reader.next();
                value.append(c);
                state = dfa.transitions[state].get(c);
            }
			
            if (dfa.finals[state] != null) {
                if (state == 0) {
					value.append((char) reader.next());
					return factory.create(invalid, value.toString());
				}
				
				return factory.create(dfa.finals[state], value.toString());
            } else {
				if (reader.peek() < 0 && value.length() == 0)
					return factory.create(eof, ""); // happens on comments at the end of files
				
				value.append((char) reader.next());
				return factory.create(invalid, value.toString());
            }
        } catch (IOException ex) {
			throw new CompileException(getPosition(), CompileExceptionCode.INTERNAL_ERROR, ex.getMessage());
        }
    }
}
