/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.project;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.zenscript.ide.host.IDEModule;
import org.openzen.zenscript.ide.host.IDEPropertyDirectory;
import org.openzen.zenscript.ide.ui.icons.ModuleIcon;

/**
 * @author Hoofdgebruiker
 */
public class ModuleTreeNode extends PackageTreeNode {
	public ModuleTreeNode(ProjectBrowser projectBrowser, IDEModule module, IDEPropertyDirectory treeState) {
		super(projectBrowser, module.getRootPackage(), treeState);

		init(module, treeState);
	}

	@Override
	public Kind getKind() {
		return Kind.MODULE;
	}

	@Override
	public DColorableIcon getIcon() {
		return ModuleIcon.INSTANCE;
	}

	@Override
	public String getTitle() {
		return module.getName() + " [" + module.getType().getName() + "]";
	}

	@Override
	public void onMouseClick(DMouseEvent e) {
		browser.setContextModule(module);
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
}
