/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.javashared.JavaCompiledModule;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceModule extends JavaCompiledModule {
	public final List<SourceFile> sourceFiles = new ArrayList<>();
	
	public JavaSourceModule(Module module) {
		super(module);
	}
	
	public void addFile(String filename, String content) {
		if (content == null)
			return;
		
		sourceFiles.add(new SourceFile(filename, content));
	}
	
	public static class SourceFile {
		public final String filename;
		public final String content;
		
		public SourceFile(String filename, String content) {
			this.filename = filename;
			this.content = content;
		}
	}
}
