/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.project;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.LiveMappedList;
import org.openzen.zenscript.ide.host.IDELibrary;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.icons.LibraryIcon;

/**
 *
 * @author Hoofdgebruiker
 */
public class LibraryTreeNode extends ProjectOverviewNode {
	private final IDELibrary library;
	private final LiveList<ProjectOverviewNode> modules;
	
	public LibraryTreeNode(ProjectBrowser browser, IDELibrary library) {
		this.library = library;
		
		modules = new LiveMappedList<>(library.getModules(), module -> new ModuleTreeNode(browser, module));
	}
	
	@Override
	public void close() {
		modules.close();
	}
	
	@Override
	public DColorableIcon getIcon() {
		return LibraryIcon.INSTANCE;
	}

	@Override
	public Kind getKind() {
		return Kind.PROJECT;
	}

	@Override
	public String getTitle() {
		return library.getName();
	}

	@Override
	public LiveList<ProjectOverviewNode> getChildren() {
		return modules;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
}
