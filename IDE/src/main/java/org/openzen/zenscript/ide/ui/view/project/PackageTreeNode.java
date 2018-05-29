/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.project;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.live.LiveConcatList;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.LiveMappedList;
import org.openzen.zenscript.ide.host.IDEPackage;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.icons.ColorableFolderIcon;

/**
 *
 * @author Hoofdgebruiker
 */
public class PackageTreeNode extends ProjectOverviewNode {
	private final IDEPackage pkg;
	private final LiveList<ProjectOverviewNode> contents;
	
	public PackageTreeNode(IDEWindow window, IDEPackage pkg) {
		this.pkg = pkg;
		
		contents = new LiveConcatList<>(
				new LiveMappedList<>(pkg.getSubPackages(), sub -> new PackageTreeNode(window, sub)),
				new LiveMappedList<>(pkg.getSourceFiles(), source -> new SourceFileTreeNode(window, source))
		);
	}

	@Override
	public LiveList<ProjectOverviewNode> getChildren() {
		return contents;
	}

	@Override
	public Kind getKind() {
		return Kind.PACKAGE;
	}

	@Override
	public DColorableIcon getIcon() {
		return ColorableFolderIcon.INSTANCE;
	}

	@Override
	public String getTitle() {
		return pkg.getName();
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
}
