/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.shared;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Hoofdgebruiker
 */
public class FileSourceFile implements SourceFile {
	public final String name;
	public final File file;
	
	public FileSourceFile(String name, File file) {
		this.name = name;
		this.file = file;
	}

	@Override
	public String getFilename() {
		return name;
	}

	@Override
	public Reader open() throws IOException {
		return new InputStreamReader(
				new BufferedInputStream(new FileInputStream(file)),
				StandardCharsets.UTF_8);
	}

	@Override
	public void update(String content) throws IOException {
		try (OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file, false)), StandardCharsets.UTF_8)) {
			writer.write(content);
		}
	}
}
