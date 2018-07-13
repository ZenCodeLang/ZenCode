/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DEmptyView;
import org.openzen.drawablegui.DHorizontalLayout;
import org.openzen.drawablegui.scroll.DScrollPane;
import org.openzen.drawablegui.DSideLayout;
import org.openzen.drawablegui.DVerticalLayout;
import org.openzen.drawablegui.border.DEmptyBorder;
import org.openzen.drawablegui.live.ImmutableLiveString;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.SimpleLiveString;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylesheetBuilder;
import org.openzen.drawablegui.tree.DTreeView;
import org.openzen.drawablegui.tree.DTreeViewStyle;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.ui.IDEDockWindow;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.dialog.CreatePackageDialog;
import org.openzen.zenscript.ide.ui.dialog.CreateSourceFileDialog;
import org.openzen.zenscript.ide.ui.icons.AddBoxIcon;
import org.openzen.zenscript.ide.ui.view.aspectbar.AspectBarView;
import org.openzen.zenscript.ide.ui.view.editor.SourceEditor;
import org.openzen.zenscript.ide.ui.view.project.RootTreeNode;

/**
 *
 * @author Hoofdgebruiker
 */
public final class WindowView extends DSideLayout {
	private final IDEWindow window;
	private final TabbedView tabs;
	public final LiveString status = new SimpleLiveString("IDE initialized");
	
	public WindowView(IDEWindow window, DevelopmentHost host) {
		super(DStyleClass.EMPTY, DEmptyView.INSTANCE);
		this.window = window;
		
		DStyleClass minimalButtonPadding = DStyleClass.inline(new DStylesheetBuilder()
				.dimensionDp("margin", 3)
				.dimensionDp("padding", 0)
				.build());
		
		IconButtonControl addPackageButton = new IconButtonControl(
				minimalButtonPadding,
				AddBoxIcon.BLUE,
				AddBoxIcon.GRAY,
				window.addContentDisabled,
				new ImmutableLiveString("Create package"),
				e -> {
					CreatePackageDialog dialog = new CreatePackageDialog(window.contextModule.getValue(), window.contextPackage.getValue());
					dialog.open(e.window);
				});
		IconButtonControl addFileButton = new IconButtonControl(
				minimalButtonPadding,
				AddBoxIcon.ORANGE,
				AddBoxIcon.GRAY,
				window.addContentDisabled,
				new ImmutableLiveString("Create source file"),
				e -> {
					CreateSourceFileDialog dialog = new CreateSourceFileDialog(window, window.contextModule.getValue(), window.contextPackage.getValue());
					dialog.open(e.window);
				});
		
		DHorizontalLayout toolbar = new DHorizontalLayout(
				DStyleClass.inline(new DStylesheetBuilder()
						.border("border", context -> DEmptyBorder.INSTANCE)
						.dimensionPx("spacing", 0)
						.color("backgroundColor", 0xFFFFFFFF)
						.build()),
				DHorizontalLayout.Alignment.LEFT,
				new DHorizontalLayout.Element(addPackageButton, 0, 0, DHorizontalLayout.ElementAlignment.TOP),
				new DHorizontalLayout.Element(addFileButton, 0, 0, DHorizontalLayout.ElementAlignment.TOP));
		DTreeView projectTree = new DTreeView(DTreeViewStyle.DEFAULT, new RootTreeNode(window, host), false);
		projectTree.getSizing().setValue(new DSizing(500, 500));
		
		DVerticalLayout projectView = new DVerticalLayout(
				DStyleClass.inline(new DStylesheetBuilder()
						.border("border", context -> DEmptyBorder.INSTANCE)
						.dimensionPx("spacing", 0)
						.build()),
				DVerticalLayout.Alignment.TOP,
				new DVerticalLayout.Element(toolbar, 0, 0, DVerticalLayout.ElementAlignment.STRETCH),
				new DVerticalLayout.Element(new DScrollPane(DStyleClass.forId("projectView"), projectTree), 1, 1, DVerticalLayout.ElementAlignment.STRETCH));
		
		setMain(tabs = new TabbedView(DStyleClass.EMPTY));
		add(Side.LEFT,projectView);
		add(Side.BOTTOM, new StatusBarView(DStyleClass.EMPTY, status));
		add(Side.TOP, new AspectBarView(DStyleClass.EMPTY, window.aspectBar));
		
		window.dockWindow.addListener(new DockWindowListener());
	}
	
	private class DockWindowListener implements IDEDockWindow.Listener {
		@Override
		public void onOpen(IDESourceFile sourceFile) {
			TabbedViewComponent tab = new TabbedViewComponent(
					sourceFile.getName(),
					null,
					new DScrollPane(DStyleClass.EMPTY, new SourceEditor(DStyleClass.EMPTY, window, sourceFile)));
			tabs.tabs.add(tab);
			tabs.currentTab.setValue(tab);
		}
	}
}
