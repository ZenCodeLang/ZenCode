/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.DAnchor;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DEmptyView;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DInputField;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.form.DForm;
import org.openzen.drawablegui.form.DFormComponent;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.LivePredicateBool;
import org.openzen.drawablegui.live.SimpleLiveBool;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.live.SimpleLiveString;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.IDEModule;
import org.openzen.zenscript.ide.host.IDEPackage;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.host.IDETarget;
import org.openzen.zenscript.ide.ui.icons.AddBoxIcon;
import org.openzen.zenscript.ide.ui.icons.BuildIcon;
import org.openzen.zenscript.ide.ui.icons.PlayIcon;
import org.openzen.zenscript.ide.ui.icons.SettingsIcon;
import org.openzen.zenscript.ide.ui.icons.ShadedProjectIcon;
import org.openzen.zenscript.ide.ui.view.IconButtonControl;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEWindow {
	private final DevelopmentHost host;
	
	public final IDEAspectBar aspectBar;
	public final IDEDockWindow dockWindow;
	public final IDEStatusBar statusBar;
	
	public IDEAspectToolbar projectToolbar;
	
	private final LiveObject<IDEModule> contextModule = new SimpleLiveObject<>(null);
	private final LiveObject<IDEPackage> contextPackage = new SimpleLiveObject<>(null);
	private final LiveObject<IDESourceFile> contextFile = new SimpleLiveObject<>(null);
	private final LiveBool addContentDisabled = new LivePredicateBool(contextPackage, pkg -> pkg == null);
	
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
		projectToolbar.controls.add(() -> new IconButtonControl(DStyleClass.EMPTY, AddBoxIcon.BLUE, AddBoxIcon.GRAY, addContentDisabled, e -> {
			DComponent input = new DInputField(DStyleClass.EMPTY, new SimpleLiveString(""), new DDpDimension(100));
			DForm root = new DForm(DStyleClass.EMPTY, new DFormComponent("Package name:", input));
			
			DUIWindow window = e.window;
			DIRectangle rectangle = window.getWindowBounds();
			DUIWindow newWindow = e.window.getContext().openDialog(rectangle.getCenterX(), rectangle.getCenterY(), DAnchor.MIDDLE_LEFT, "Create package", root);
			
			newWindow.focus(input);
		}));
		projectToolbar.controls.add(() -> new IconButtonControl(DStyleClass.EMPTY, AddBoxIcon.ORANGE, AddBoxIcon.GRAY, addContentDisabled, e -> {
			DComponent input = new DInputField(DStyleClass.EMPTY, new SimpleLiveString(""), new DDpDimension(100));
			DForm root = new DForm(DStyleClass.EMPTY, new DFormComponent("Filename:", input));
			
			DUIWindow window = e.window;
			DIRectangle rectangle = window.getWindowBounds();
			DUIWindow newWindow = e.window.getContext().openDialog(rectangle.getCenterX(), rectangle.getCenterY(), DAnchor.MIDDLE_LEFT, "Create file", root);
			
			newWindow.focus(input);
		}));
		projectToolbar.controls.add(() -> new IconButtonControl(DStyleClass.EMPTY, SettingsIcon.PURPLE, e -> {
			
		}));
		projectToolbar.controls.add(() -> new IconButtonControl(DStyleClass.EMPTY, BuildIcon.BLUE, e -> {
			for (IDETarget target : host.getTargets()) {
				if (target.canBuild())
					target.build();
			}
		}));
		projectToolbar.controls.add(() -> new IconButtonControl(DStyleClass.EMPTY, PlayIcon.GREEN, e -> {
			for (IDETarget target : host.getTargets()) {
				if (target.canRun())
					target.run();
			}
		}));
		aspectBar.toolbars.add(projectToolbar);
	}
	
	public void setContextModule(IDEModule module) {
		contextModule.setValue(module);
		contextPackage.setValue(module.getRootPackage());
		contextFile.setValue(null);
	}
	
	public void setContextPackage(IDEPackage pkg) {
		contextModule.setValue(null);
		contextPackage.setValue(pkg);
		contextFile.setValue(null);
	}
	
	public void setContextFile(IDESourceFile file) {
		contextModule.setValue(null);
		contextPackage.setValue(null);
		contextFile.setValue(file);
	}
}
