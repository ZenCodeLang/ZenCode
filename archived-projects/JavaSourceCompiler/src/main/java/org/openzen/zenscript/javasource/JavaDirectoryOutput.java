/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * @author Hoofdgebruiker
 */
public class JavaDirectoryOutput {
	private final File directory;

	public JavaDirectoryOutput(File directory) {
		this.directory = directory;

		if (!directory.exists())
			directory.mkdirs();
	}

	public void add(JavaSourceModule module) {
		for (JavaSourceModule.SourceFile file : module.sourceFiles) {
			File target = new File(directory, file.filename);
			if (!target.getParentFile().exists())
				target.getParentFile().mkdirs();

			try (Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8))) {
				output.write(file.content);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
