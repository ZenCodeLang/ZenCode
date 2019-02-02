/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import live.LiveArrayList;
import live.LiveList;
import live.MutableLiveList;
import live.SortedLiveList;

import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.constructor.module.directory.SourceDirectoryPackage;
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
		
		for (SourcePackage subPackage : pkg.getSubPackages())
			subPackages.add(new LocalPackage(subPackage));
		
		for (SourceFile sourceFile : pkg.getFiles())
			sourceFiles.add(new LocalSourceFile(sourceFile));
		
		subPackagesSorted = new SortedLiveList<>(subPackages, (a, b) -> a.getName().compareTo(b.getName()));
		sourceFilesSorted = new SortedLiveList<>(sourceFiles, (a, b) -> a.getName().getValue().compareTo(b.getName().getValue()));
	}
	
	@Override
	public String getName() {
		return pkg.getName();
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
		SourcePackage subpkg = this.pkg.createSubPackage(name);
		LocalPackage local = new LocalPackage(subpkg);
		subPackages.add(local);
		return local;
	}
	
	@Override
	public IDESourceFile createSourceFile(String name) {
		SourceFile sourceFile = pkg.createSourceFile(name);
		IDESourceFile localSourceFile = new LocalSourceFile(sourceFile);
		sourceFiles.add(localSourceFile);
		return localSourceFile;
	}
}
