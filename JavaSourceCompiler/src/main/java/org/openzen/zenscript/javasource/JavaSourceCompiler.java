/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.compiler.ZenCodeCompiler;
import org.openzen.zenscript.shared.SourceFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceCompiler implements ZenCodeCompiler {
	private final File directory;
	private final ZSPackage pkg;
	private final Map<File, JavaSourceFile> sourceFiles = new HashMap<>();
	
	public JavaSourceCompiler(File directory, ZSPackage pkg) {
		if (!directory.exists())
			directory.mkdirs();
		
		this.directory = directory;
		this.pkg = pkg;
	}
	
	@Override
	public void addDefinition(HighLevelDefinition definition) {
		File file = new File(getDirectory(definition.pkg), definition.name + ".java");
		if (definition.name == null || definition instanceof FunctionDefinition) {
			SourceFile source = definition.getTag(SourceFile.class);
			if (source != null) {
				int slash = Math.max(source.filename.lastIndexOf('/'), source.filename.lastIndexOf('\\'));
				String filename = source.filename.substring(slash < 0 ? 0 : slash);
				filename = filename.substring(0, filename.lastIndexOf('.'));
				file = new File(getDirectory(definition.pkg), filename + ".java");
			}
		}
		
		JavaSourceFile sourceFile = sourceFiles.get(file);
		if (sourceFile == null)
			sourceFiles.put(file, sourceFile = new JavaSourceFile(this, file, definition.pkg.fullName));
		
		sourceFile.add(definition);
	}
	
	@Override
	public void addScriptBlock(ScriptBlock script) {
		
	}
	
	@Override
	public void finish() {
		for (JavaSourceFile sourceFile : sourceFiles.values())
			sourceFile.write();
	}
	
	@Override
	public void run() {
		throw new UnsupportedOperationException("Cannot run this target");
	}
	
	public String getFullName(HighLevelDefinition definition) {
		return definition.pkg.fullName + "." + definition.name;
	}
	
	private File getDirectory(ZSPackage pkg) {
		if (pkg == null)
			throw new RuntimeException("Package not in the package directory");
		if (pkg == this.pkg)
			return directory;
		
		File base = getDirectory(pkg.parent);
		return new File(base, pkg.name);
	}
}
