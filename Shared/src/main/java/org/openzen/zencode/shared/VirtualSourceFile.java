/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.shared;

import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author Hoofdgebruiker
 */
public class VirtualSourceFile implements SourceFile {
	private final String filename;
	
	public VirtualSourceFile(String filename) {
		this.filename = filename;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public Reader open() throws IOException {
		throw new UnsupportedOperationException("Cannot open virtual source files");
	}
}
