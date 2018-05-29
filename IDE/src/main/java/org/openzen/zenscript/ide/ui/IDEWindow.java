/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.zenscript.ide.host.IDESourceFile;

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
	}
	
	public IDEWindow(IDEAspectBar aspectBar, IDEDockWindow dockWindow, IDEStatusBar statusBar) {
		this.aspectBar = aspectBar;
		this.dockWindow = dockWindow;
		this.statusBar = statusBar;
	}
	
	public void open(IDESourceFile sourceFile) {
		dockWindow.currentSourceFile.setValue(sourceFile);
	}
}
