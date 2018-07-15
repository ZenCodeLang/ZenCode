/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.project;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.live.LiveEmptyList;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.ui.icons.CodeIcon;

/**
 *
 * @author Hoofdgebruiker
 */
public class SourceFileTreeNode extends ProjectOverviewNode {
	private final ProjectBrowser browser;
	private final IDESourceFile sourceFile;
	
	public SourceFileTreeNode(ProjectBrowser browser, IDESourceFile sourceFile) {
		this.browser = browser;
		this.sourceFile = sourceFile;
	}
	
	@Override
	public void close() {
		
	}

	@Override
	public Kind getKind() {
		return Kind.SCRIPT;
	}

	@Override
	public DColorableIcon getIcon() {
		return CodeIcon.INSTANCE;
	}

	@Override
	public String getTitle() {
		return sourceFile.getName().getValue(); // TODO: live strings for node names
	}

	@Override
	public LiveList<ProjectOverviewNode> getChildren() {
		return LiveEmptyList.get();
	}

	@Override
	public boolean isLeaf() {
		return true;
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		browser.setContextFile(sourceFile);
		if (e.isDoubleClick()) {
			browser.open(sourceFile);
		}
	}
}
