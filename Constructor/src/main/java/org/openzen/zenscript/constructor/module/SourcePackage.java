/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Hoofdgebruiker
 */
public class SourcePackage {
	public final File directory;
	public final String name;
	public final Map<String, SourcePackage> subPackages = new HashMap<>();
	public final Map<String, SourceFile> sourceFiles = new HashMap<>();
	
	public SourcePackage(File directory, String name) {
		this.directory = directory;
		this.name = name;
	}
	
	public void addPackage(SourcePackage pkg) {
		subPackages.put(pkg.name, pkg);
	}
	
	public void addFile(SourceFile file) {
		sourceFiles.put(file.name, file);
	}
}
