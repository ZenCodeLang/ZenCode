/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DEmptyView;
import org.openzen.drawablegui.scroll.DScrollPane;
import org.openzen.drawablegui.DSideLayout;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.SimpleLiveString;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.tree.DTreeView;
import org.openzen.drawablegui.tree.DTreeViewStyle;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.ui.IDEDockWindow;
import org.openzen.zenscript.ide.ui.IDEWindow;
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
		
		DTreeView projectView = new DTreeView(DTreeViewStyle.DEFAULT, new RootTreeNode(window, host), false);
		projectView.getDimensionPreferences().setValue(new DDimensionPreferences(500, 500));
		setMain(tabs = new TabbedView(DStyleClass.EMPTY));
		add(Side.LEFT, new DScrollPane(DStyleClass.forId("projectView"), projectView));
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
