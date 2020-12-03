package org.openzen.zenscript.constructor.module.directory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.constructor.module.SourcePackage;

public class SourceDirectoryPackage implements SourcePackage {
	public final File directory;
	public final String name;
	public final Map<String, SourceDirectoryPackage> subPackages = new HashMap<>();
	public final Map<String, SourceFile> sourceFiles = new HashMap<>();
	
	public SourceDirectoryPackage(File directory, String name) {
		this.directory = directory;
		this.name = name;
		
		for (File file : directory.listFiles()) {
			if (file.isDirectory())
				subPackages.put(file.getName(), new SourceDirectoryPackage(file, file.getName()));
			else if (file.isFile() && file.getName().endsWith(".zs"))
				sourceFiles.put(file.getName(), new FileSourceFile(file.getName(), file));
		}
	}
	
	public void addPackage(SourceDirectoryPackage pkg) {
		subPackages.put(pkg.name, pkg);
	}
	
	public void addFile(SourceFile file) {
		sourceFiles.put(file.getFilename(), file);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<? extends SourcePackage> getSubPackages() {
		return subPackages.values();
	}

	@Override
	public Collection<? extends SourceFile> getFiles() {
		return sourceFiles.values();
	}

	@Override
	public SourcePackage createSubPackage(String name) {
		File file = new File(directory, name);
		file.mkdir();
		
		SourceDirectoryPackage sourcePackage = new SourceDirectoryPackage(file, name);
		subPackages.put(name, sourcePackage);
		return sourcePackage;
	}

	@Override
	public SourceFile createSourceFile(String name) {
		File file = new File(directory, name);
		try {
			file.createNewFile();
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO
		}
		
		FileSourceFile sourceFile = new FileSourceFile(name, file);
		sourceFiles.put(name, sourceFile);
		return sourceFile;
	}
}
