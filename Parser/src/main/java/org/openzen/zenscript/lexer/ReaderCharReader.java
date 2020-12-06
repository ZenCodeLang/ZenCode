/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Hoofdgebruiker
 */
public class ReaderCharReader implements CharReader {
	private final Reader reader;
	private int next;

	public ReaderCharReader(Reader reader) throws IOException {
		this.reader = reader;
		next = reader.read();
	}

	@Override
	public int peek() {
		return next;
	}

	@Override
	public int next() throws IOException {
		int result = next;
		next = reader.read();
		return result;
	}
}
