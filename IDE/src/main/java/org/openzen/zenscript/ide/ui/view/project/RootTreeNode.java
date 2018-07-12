/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.project;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.LiveMappedList;
import org.openzen.drawablegui.live.LivePrefixedList;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.icons.ProjectIcon;

/**
 *
 * @author Hoofdgebruiker
 */
public class RootTreeNode extends ProjectOverviewNode {
	private final DevelopmentHost host;
	private final LiveList<ProjectOverviewNode> children;
	
	public RootTreeNode(IDEWindow window, DevelopmentHost host) {
		this.host = host;
		
		children = new LivePrefixedList<>(
				new ProjectTreeNode(window, host),
				new LiveMappedList<>(host.getLibraries(), library -> new LibraryTreeNode(window, library)));
	}
	
	@Override
	public void close() {
		children.close();
	}
	
	@Override
	public DColorableIcon getIcon() {
		return ProjectIcon.INSTANCE;
	}
	
	@Override
	public Kind getKind() {
		return Kind.ROOT;
	}

	@Override
	public String getTitle() {
		return host.getName();
	}

	@Override
	public LiveList<ProjectOverviewNode> getChildren() {
		return children;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
}
