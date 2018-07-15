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
import org.openzen.zenscript.ide.ui.icons.ProjectIcon;

/**
 *
 * @author Hoofdgebruiker
 */
public class ProjectTreeNode extends ProjectOverviewNode {
	private final ProjectBrowser browser;
	private final DevelopmentHost host;
	private final LiveList<ProjectOverviewNode> modules;
	
	public ProjectTreeNode(ProjectBrowser browser, DevelopmentHost host) {
		this.browser = browser;
		this.host = host;
		
		modules = new LiveMappedList<>(host.getModules(), module -> new ModuleTreeNode(browser, module));
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
		browser.setContextProject();
	}
}
