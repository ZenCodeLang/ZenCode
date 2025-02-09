/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.project;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DMouseEvent;
import live.LiveConcatList;
import live.LiveList;
import live.LiveMappedList;
import org.openzen.zenscript.ide.host.IDEModule;
import org.openzen.zenscript.ide.host.IDEPackage;
import org.openzen.zenscript.ide.host.IDEPropertyDirectory;
import org.openzen.zenscript.ide.ui.icons.FolderIcon;

/**
 * @author Hoofdgebruiker
 */
public class PackageTreeNode extends ProjectOverviewNode {
	protected final ProjectBrowser browser;
	private final IDEPackage pkg;
	protected IDEModule module;
	private LiveList<ProjectOverviewNode> contents;

	public PackageTreeNode(ProjectBrowser browser, IDEPackage pkg, IDEPropertyDirectory treeState) {
		super(treeState.getLiveBool("collapsed", true));

		this.browser = browser;
		this.pkg = pkg;
	}

	public PackageTreeNode(ProjectBrowser browser, IDEModule module, IDEPackage pkg, IDEPropertyDirectory treeState) {
		super(treeState.getLiveBool("collapsed", true));

		this.browser = browser;
		this.module = module;
		this.pkg = pkg;

		init(module, treeState);
	}

	protected final void init(IDEModule module, IDEPropertyDirectory treeState) {
		this.module = module;
		contents = new LiveConcatList<>(
				new LiveMappedList<>(pkg.getSubPackages(), sub -> new PackageTreeNode(browser, module, sub, treeState.getSubdirectory(sub.getName()))),
				new LiveMappedList<>(pkg.getSourceFiles(), source -> new SourceFileTreeNode(browser, source))
		);
	}

	@Override
	public void close() {
		contents.close();
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
		return FolderIcon.INSTANCE;
	}

	@Override
	public String getTitle() {
		return pkg.getName();
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public void onMouseClick(DMouseEvent e) {
		browser.setContextPackage(module, pkg);
	}
}
