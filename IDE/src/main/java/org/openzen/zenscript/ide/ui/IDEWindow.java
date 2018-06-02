/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.live.SimpleLiveBool;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.ui.icons.AddBoxIcon;
import org.openzen.zenscript.ide.ui.icons.ProjectIcon;
import org.openzen.zenscript.ide.ui.icons.SettingsIcon;
import org.openzen.zenscript.ide.ui.view.IconButtonControl;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEWindow {
	public final IDEAspectBar aspectBar;
	public final IDEDockWindow dockWindow;
	public final IDEStatusBar statusBar;
	
	public IDEWindow() {
		aspectBar = new IDEAspectBar();
		dockWindow = new IDEDockWindow();
		statusBar = new IDEStatusBar();
		init();
	}
	
	public IDEWindow(IDEAspectBar aspectBar, IDEDockWindow dockWindow, IDEStatusBar statusBar) {
		this.aspectBar = aspectBar;
		this.dockWindow = dockWindow;
		this.statusBar = statusBar;
		init();
	}
	
	public void open(IDESourceFile sourceFile) {
		dockWindow.currentSourceFile.setValue(sourceFile);
	}
	
	private void init() {
		IDEAspectToolbar projectToolbar = new IDEAspectToolbar(0, ProjectIcon.GREY, "Project", "Project management");
		projectToolbar.controls.add(() -> new IconButtonControl(DStyleClass.EMPTY, AddBoxIcon.BLACK, e -> {}));
		projectToolbar.controls.add(() -> new IconButtonControl(DStyleClass.EMPTY, SettingsIcon.BLACK, e -> {}));
		aspectBar.addToolbar(projectToolbar);
	}
}
