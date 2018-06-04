/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import java.util.Arrays;
import org.openzen.drawablegui.live.LiveArrayList;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.zenscript.constructor.module.SourcePackage;
import org.openzen.zenscript.ide.host.IDEPackage;
import org.openzen.zenscript.ide.host.IDESourceFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class LocalPackage implements IDEPackage {
	private final SourcePackage pkg;
	private final LiveList<IDEPackage> subPackages = new LiveArrayList<>();
	private final LiveList<IDESourceFile> sourceFiles = new LiveArrayList<>();
	
	public LocalPackage(SourcePackage pkg) {
		this.pkg = pkg;
		
		String[] subPackageKeys = pkg.subPackages.keySet()
				.toArray(new String[pkg.subPackages.size()]);
		Arrays.sort(subPackageKeys);
		
		String[] sourceFileKeys = pkg.sourceFiles.keySet()
				.toArray(new String[pkg.sourceFiles.size()]);
		Arrays.sort(sourceFileKeys);
		
		for (String subKey : subPackageKeys) {
			subPackages.add(new LocalPackage(pkg.subPackages.get(subKey)));
		}
		for (String sourceFileKey : sourceFileKeys) {
			sourceFiles.add(new LocalSourceFile(pkg.sourceFiles.get(sourceFileKey)));
		}
	}
	
	@Override
	public String getName() {
		return pkg.name;
	}

	@Override
	public LiveList<IDEPackage> getSubPackages() {
		return subPackages;
	}

	@Override
	public LiveList<IDESourceFile> getSourceFiles() {
		return sourceFiles;
	}
}
