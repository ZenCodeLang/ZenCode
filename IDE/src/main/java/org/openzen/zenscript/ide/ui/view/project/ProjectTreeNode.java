/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.project;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.LiveMappedList;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.icons.ProjectIcon;

/**
 *
 * @author Hoofdgebruiker
 */
public class ProjectTreeNode extends ProjectOverviewNode {
	private final IDEWindow window;
	private final DevelopmentHost host;
	private final LiveList<ProjectOverviewNode> modules;
	
	public ProjectTreeNode(IDEWindow window, DevelopmentHost host) {
		this.window = window;
		this.host = host;
		
		modules = new LiveMappedList<>(host.getModules(), module -> new ModuleTreeNode(window, module));
	}
	
	@Override
	public void close(){
		modules.close();
	}
	
	@Override
	public DColorableIcon getIcon() {
		return ProjectIcon.INSTANCE;
	}

	@Override
	public Kind getKind() {
		return Kind.PROJECT;
	}

	@Override
	public String getTitle() {
		return host.getName();
	}

	@Override
	public LiveList<ProjectOverviewNode> getChildren() {
		return modules;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		window.aspectBar.active.setValue(window.projectToolbar);
	}
}
