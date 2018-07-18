/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.MutableLiveString;
import org.openzen.drawablegui.live.SimpleLiveString;
import org.openzen.zenscript.constructor.module.SourceFile;
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
		this.name = new SimpleLiveString(file.name);
	}

	@Override
	public LiveString getName() {
		return name;
	}

	@Override
	public Reader read() throws IOException {
		return new InputStreamReader(
				new BufferedInputStream(new FileInputStream(file.file)),
				StandardCharsets.UTF_8);
	}

	@Override
	public void update(String content) {
		try {
			Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file.file)), StandardCharsets.UTF_8);
			writer.write(content);
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
