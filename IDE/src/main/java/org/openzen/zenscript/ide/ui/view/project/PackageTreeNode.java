/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.project;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.live.LiveConcatList;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.LiveMappedList;
import org.openzen.zenscript.ide.host.IDEModule;
import org.openzen.zenscript.ide.host.IDEPackage;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.icons.FolderIcon;

/**
 *
 * @author Hoofdgebruiker
 */
public class PackageTreeNode extends ProjectOverviewNode {
	protected final IDEWindow window;
	protected IDEModule module;
	private final IDEPackage pkg;
	private LiveList<ProjectOverviewNode> contents;
	
	public PackageTreeNode(IDEWindow window, IDEPackage pkg) {
		this.window = window;
		this.pkg = pkg;
	}
	
	public PackageTreeNode(IDEWindow window, IDEModule module, IDEPackage pkg) {
		this.window = window;
		this.module = module;
		this.pkg = pkg;
		
		init(module);
	}
	
	protected final void init(IDEModule module) {
		this.module = module;
		contents = new LiveConcatList<>(
				new LiveMappedList<>(pkg.getSubPackages(), sub -> new PackageTreeNode(window, module, sub)),
				new LiveMappedList<>(pkg.getSourceFiles(), source -> new SourceFileTreeNode(window, source))
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
		window.setContextPackage(module, pkg);
	}
}
