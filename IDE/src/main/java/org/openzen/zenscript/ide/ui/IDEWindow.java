/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.ui.icons.ColorableAddBoxIcon;
import org.openzen.zenscript.ide.ui.icons.ColorableProjectIcon;
import org.openzen.zenscript.ide.ui.icons.ColorableSettingsIcon;
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
		IDEAspectToolbar projectToolbar = new IDEAspectToolbar(0, ColorableProjectIcon.INSTANCE, "Project", "Project management");
		projectToolbar.controls.add(() -> new IconButtonControl(ColorableAddBoxIcon.BLACK, e -> {}));
		projectToolbar.controls.add(() -> new IconButtonControl(ColorableSettingsIcon.BLACK, e -> {}));
		aspectBar.addToolbar(projectToolbar);
	}
}
