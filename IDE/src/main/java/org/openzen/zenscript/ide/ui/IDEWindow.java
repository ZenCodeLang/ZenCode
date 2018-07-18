/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.live.ImmutableLiveString;
import org.openzen.drawablegui.live.LiveArrayList;
import org.openzen.drawablegui.live.MutableLiveList;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.host.IDETarget;
import org.openzen.zenscript.ide.ui.icons.BuildIcon;
import org.openzen.zenscript.ide.ui.icons.PlayIcon;
import org.openzen.zenscript.ide.ui.icons.SettingsIcon;
import org.openzen.zenscript.ide.ui.icons.ShadedProjectIcon;
import org.openzen.zenscript.ide.ui.view.IconButtonControl;
import org.openzen.zenscript.ide.ui.view.output.OutputLine;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEWindow {
	private final DevelopmentHost host;
	
	public final IDEAspectBar aspectBar;
	public final IDEDockWindow dockWindow;
	public final IDEStatusBar statusBar;
	public final MutableLiveList<OutputLine> output = new LiveArrayList<>();
	
	public IDEAspectToolbar projectToolbar;
	
	public IDEWindow(DevelopmentHost host) {
		this.host = host;
		
		aspectBar = new IDEAspectBar();
		dockWindow = new IDEDockWindow();
		statusBar = new IDEStatusBar();
		init();
	}
	
	public IDEWindow(DevelopmentHost host, IDEAspectBar aspectBar, IDEDockWindow dockWindow, IDEStatusBar statusBar) {
		this.host = host;
		
		this.aspectBar = aspectBar;
		this.dockWindow = dockWindow;
		this.statusBar = statusBar;
		init();
	}
	
	public void open(IDESourceFile sourceFile) {
		dockWindow.open(sourceFile);
	}
	
	private void init() {
		projectToolbar = new IDEAspectToolbar(0, ShadedProjectIcon.PURPLE, "Project", "Project management");
		projectToolbar.controls.add(() -> new IconButtonControl(DStyleClass.EMPTY, SettingsIcon.PURPLE, new ImmutableLiveString("Project settings"), e -> {
			
		}));
		projectToolbar.controls.add(() -> new IconButtonControl(DStyleClass.EMPTY, BuildIcon.BLUE, new ImmutableLiveString("Build"), e -> {
			for (IDETarget target : host.getTargets()) {
				if (target.canBuild())
					target.build(line -> output.add(line));
			}
		}));
		projectToolbar.controls.add(() -> new IconButtonControl(DStyleClass.EMPTY, PlayIcon.GREEN, new ImmutableLiveString("Run"), e -> {
			for (IDETarget target : host.getTargets()) {
				if (target.canRun())
					target.run(line -> output.add(line));
			}
		}));
		aspectBar.toolbars.add(projectToolbar);
	}
}
