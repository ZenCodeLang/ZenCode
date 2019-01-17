/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import live.LiveArrayList;
import live.LiveList;
import live.MutableLiveList;
import live.SortedLiveList;

import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.constructor.module.SourcePackage;
import org.openzen.zenscript.ide.host.IDEPackage;
import org.openzen.zenscript.ide.host.IDESourceFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class LocalPackage implements IDEPackage {
	private final SourcePackage pkg;
	private final MutableLiveList<IDEPackage> subPackages = new LiveArrayList<>();
	private final MutableLiveList<IDESourceFile> sourceFiles = new LiveArrayList<>();
	
	private final LiveList<IDEPackage> subPackagesSorted;
	private final LiveList<IDESourceFile> sourceFilesSorted;
	
	public LocalPackage(SourcePackage pkg) {
		this.pkg = pkg;
		
		String[] subPackageKeys = pkg.subPackages.keySet()
				.toArray(new String[pkg.subPackages.size()]);
		Arrays.sort(subPackageKeys);
		
		String[] sourceFileKeys = pkg.sourceFiles.keySet()
				.toArray(new String[pkg.sourceFiles.size()]);
		Arrays.sort(sourceFileKeys);
		
		for (SourcePackage subPackage : pkg.subPackages.values()) {
			subPackages.add(new LocalPackage(subPackage));
		}
		for (SourceFile sourceFile : pkg.sourceFiles.values()) {
			sourceFiles.add(new LocalSourceFile(sourceFile));
		}
		
		subPackagesSorted = new SortedLiveList<>(subPackages, (a, b) -> a.getName().compareTo(b.getName()));
		sourceFilesSorted = new SortedLiveList<>(sourceFiles, (a, b) -> a.getName().getValue().compareTo(b.getName().getValue()));
	}
	
	@Override
	public String getName() {
		return pkg.name;
	}

	@Override
	public LiveList<IDEPackage> getSubPackages() {
		return subPackagesSorted;
	}

	@Override
	public LiveList<IDESourceFile> getSourceFiles() {
		return sourceFilesSorted;
	}
	
	@Override
	public IDEPackage createSubPackage(String name) {
		File file = new File(this.pkg.directory, name);
		file.mkdir();
		
		SourcePackage sourcePackage = new SourcePackage(file, name);
		IDEPackage pkg = new LocalPackage(sourcePackage);
		this.pkg.subPackages.put(name, sourcePackage);
		subPackages.add(pkg);
		return pkg;
	}
	
	@Override
	public IDESourceFile createSourceFile(String name) {
		File file = new File(this.pkg.directory, name);
		try {
			file.createNewFile();
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO
		}
		
		FileSourceFile sourceFile = new FileSourceFile(name, file);
		IDESourceFile localSourceFile = new LocalSourceFile(sourceFile);
		this.pkg.sourceFiles.put(name, sourceFile);
		sourceFiles.add(localSourceFile);
		return localSourceFile;
	}
}
