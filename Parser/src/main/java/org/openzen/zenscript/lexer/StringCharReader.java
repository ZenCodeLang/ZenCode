/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import java.io.IOException;

/**
 *
 * @author Hoofdgebruiker
 */
public class StringCharReader implements CharReader {
	private final char[] data;
	private int index;
	
	public StringCharReader(String data) {
		this.data = data.toCharArray();
		this.index = 0;
	}
	
	@Override
	public int peek() {
		return index >= data.length ? -1 : data[index];
	}

	@Override
	public int next() throws IOException {
		return index >= data.length ? -1 : data[index++];
	}
}
