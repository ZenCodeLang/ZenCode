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
public class WhitespaceFilteringParser<TT extends TokenType, T extends Token<TT>> implements TokenStream<TT, T> {
	private final TokenStream<TT, T> stream;
	private T next;
	private CodePosition position;
	private String whitespace;
	
	public WhitespaceFilteringParser(TokenStream<TT, T> stream) throws ParseException {
		this.stream = stream;
		advance();
	}
	
	public String getLastWhitespace() {
		return whitespace;
	}
	
	public void skipWhitespaceNewline() {
		int index = whitespace.indexOf('\n');
		if (index >= 0)
			whitespace = whitespace.substring(index + 1);
	}
	
	public String grabWhitespaceLine() {
		if (whitespace.contains("\n")) {
			int index = whitespace.indexOf('\n');
			String result = whitespace.substring(0, index);
			whitespace = whitespace.substring(index + 1);
			return result;
		} else {
			String result = whitespace;
			whitespace = "";
			return result;
		}
	}
	
	public T peek() {
		return next;
	}
	
	public void replace(T other) {
		next = other;
	}

	@Override
	public T next() throws ParseException {
		T result = next;
		advance();
		return result;
	}

	@Override
	public CodePosition getPosition() {
		return position;
	}

	@Override
	public TT getEOF() {
		return stream.getEOF();
	}
	
	private void advance() throws ParseException {
		whitespace = "";
		position = stream.getPosition();
		next = stream.next();
		while (next.getType().isWhitespace()) {
			whitespace += next.getContent();
			position = stream.getPosition();
			next = stream.next();
		}
	}
}
