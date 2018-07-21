/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.shared;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 *
 * @author Hoofdgebruiker
 */
public class LiteralSourceFile implements SourceFile {
	private final String filename;
	private final String contents;
	
	public LiteralSourceFile(String filename, String contents) {
		this.filename = filename;
		this.contents = contents;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public Reader open() throws IOException {
		return new StringReader(contents);
	}

	@Override
	public void update(String content) throws IOException {
		throw new UnsupportedOperationException("Cannot update literal source files");
	}
}
