/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.project;

import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.layout.DHorizontalLayout;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.layout.DVerticalLayout;
import org.openzen.drawablegui.border.DEmptyBorder;
import org.openzen.drawablegui.border.DPaddedBorder;
import org.openzen.drawablegui.live.ImmutableLiveString;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LivePredicateBool;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.scroll.DScrollPane;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylesheetBuilder;
import org.openzen.drawablegui.tree.DTreeView;
import org.openzen.drawablegui.tree.DTreeViewStyle;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.IDEModule;
import org.openzen.zenscript.ide.host.IDEPackage;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.dialog.CreatePackageDialog;
import org.openzen.zenscript.ide.ui.dialog.CreateSourceFileDialog;
import org.openzen.zenscript.ide.ui.icons.AddBoxIcon;
import org.openzen.zenscript.ide.ui.view.IconButtonControl;

/**
 *
 * @author Hoofdgebruiker
 */
public class ProjectBrowser {
	public final IDEWindow window;
	public final DComponent view;
	
	public final MutableLiveObject<IDEModule> contextModule = new SimpleLiveObject<>(null);
	public final MutableLiveObject<IDEPackage> contextPackage = new SimpleLiveObject<>(null);
	public final MutableLiveObject<IDESourceFile> contextFile = new SimpleLiveObject<>(null);
	public final LiveBool addContentDisabled = new LivePredicateBool(contextPackage, pkg -> pkg == null);
	
	public ProjectBrowser(IDEWindow window, DevelopmentHost host) {
		this.window = window;
		
		DStyleClass minimalButtonPadding = DStyleClass.inline(new DStylesheetBuilder()
				.dimensionDp("margin", 3)
				.dimensionDp("padding", 0)
				.build());
		
		IconButtonControl addPackageButton = new IconButtonControl(
				minimalButtonPadding,
				AddBoxIcon.BLUE,
				AddBoxIcon.GRAY,
				addContentDisabled,
				new ImmutableLiveString("Create package"),
				e -> {
					CreatePackageDialog dialog = new CreatePackageDialog(contextModule.getValue(), contextPackage.getValue());
					dialog.open(e.window);
				});
		IconButtonControl addFileButton = new IconButtonControl(
				minimalButtonPadding,
				AddBoxIcon.ORANGE,
				AddBoxIcon.GRAY,
				addContentDisabled,
				new ImmutableLiveString("Create source file"),
				e -> {
					CreateSourceFileDialog dialog = new CreateSourceFileDialog(window, contextModule.getValue(), contextPackage.getValue());
					dialog.open(e.window);
				});
		
		DHorizontalLayout toolbar = new DHorizontalLayout(
				DStyleClass.inline(new DStylesheetBuilder()
						.border("border", context -> new DPaddedBorder(0, context.dp(2), 0, 0))
						.dimensionPx("spacing", 0)
						.build()),
				DHorizontalLayout.Alignment.LEFT,
				new DHorizontalLayout.Element(addPackageButton, 0, 0, DHorizontalLayout.ElementAlignment.TOP),
				new DHorizontalLayout.Element(addFileButton, 0, 0, DHorizontalLayout.ElementAlignment.TOP));
		DTreeView projectTree = new DTreeView(
				DTreeViewStyle.DEFAULT,
				new RootTreeNode(this, host), false);
		projectTree.getSizing().setValue(new DSizing(500, 500));
		
		view = new DVerticalLayout(
				DStyleClass.inline(new DStylesheetBuilder()
						.border("border", DEmptyBorder.ELEMENT)
						.dimensionPx("spacing", 0)
						.marginDp("margin", 3)
						.dimensionDp("cornerRadius", 2)
						.color("backgroundColor", 0xFFFFFFFF)
						.shadow("shadow", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 3 * context.getScale()))
						.build()),
				DVerticalLayout.Alignment.TOP,
				new DVerticalLayout.Element(toolbar, 0, 0, DVerticalLayout.ElementAlignment.STRETCH),
				new DVerticalLayout.Element(new DScrollPane(DStyleClass.forId("projectView"), projectTree), 1, 1, DVerticalLayout.ElementAlignment.STRETCH));
	}
	
	public void setContextModule(IDEModule module) {
		contextModule.setValue(module);
		contextPackage.setValue(module.getRootPackage());
		contextFile.setValue(null);
	}
	
	public void setContextPackage(IDEModule module, IDEPackage pkg) {
		contextModule.setValue(module);
		contextPackage.setValue(pkg);
		contextFile.setValue(null);
	}
	
	public void setContextFile(IDESourceFile file) {
		contextModule.setValue(null);
		contextPackage.setValue(null);
		contextFile.setValue(file);
	}
	
	public void setContextProject() {
		contextModule.setValue(null);
		contextPackage.setValue(null);
		contextFile.setValue(null);
		window.aspectBar.active.setValue(window.projectToolbar);
	}
	
	public void open(IDESourceFile file) {
		window.open(file);
	}
}
