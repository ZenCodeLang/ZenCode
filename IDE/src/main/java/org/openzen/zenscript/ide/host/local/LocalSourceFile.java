/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import live.LiveString;
import live.MutableLiveString;
import live.SimpleLiveString;

import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.ide.host.IDESourceFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class LocalSourceFile implements IDESourceFile {
	private final SourceFile file;
	private final MutableLiveString name;
	
	public LocalSourceFile(SourceFile file) {
		this.file = file;
		this.name = new SimpleLiveString(file.getFilename());
	}

	@Override
	public LiveString getName() {
		return name;
	}

	@Override
	public SourceFile getFile() {
		return file;
	}

	@Override
	public void update(String content) {
		try {
			if (file instanceof FileSourceFile) {
				Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(((FileSourceFile)file).file)), StandardCharsets.UTF_8);
				writer.write(content);
				writer.close();
			} else {
				throw new UnsupportedOperationException("Cannot write to a non-file source file!");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
