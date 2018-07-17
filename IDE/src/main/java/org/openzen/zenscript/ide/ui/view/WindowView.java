/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DEmptyView;
import org.openzen.drawablegui.border.DLineBorder;
import org.openzen.drawablegui.scroll.DScrollPane;
import org.openzen.drawablegui.layout.DSideLayout;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.SimpleLiveInt;
import org.openzen.drawablegui.live.SimpleLiveString;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylesheetBuilder;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.ui.IDEDockWindow;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.view.aspectbar.AspectBarView;
import org.openzen.zenscript.ide.ui.view.editor.SourceEditor;
import org.openzen.zenscript.ide.ui.view.project.ProjectBrowser;

/**
 *
 * @author Hoofdgebruiker
 */
public final class WindowView extends DSideLayout {
	private final IDEWindow window;
	private final TabbedView tabs;
	public final LiveString status = new SimpleLiveString("IDE initialized");
	private final ProjectBrowser projectBrowser;
	
	public WindowView(IDEWindow window, DevelopmentHost host) {
		super(DStyleClass.inline(
				new DStylesheetBuilder().color("backgroundColor", 0xFFEEEEEE).build()),
				DEmptyView.INSTANCE);
		this.window = window;
		
		projectBrowser = new ProjectBrowser(window, host);
		
		setMain(tabs = new TabbedView(DStyleClass.inline(new DStylesheetBuilder()
				.marginDp("margin", 3)
				.build())));
		add(Side.LEFT, projectBrowser.view);
		add(Side.BOTTOM, new StatusBarView(DStyleClass.EMPTY, status));
		add(Side.TOP, new AspectBarView(DStyleClass.EMPTY, window.aspectBar));
		
		window.dockWindow.addListener(new DockWindowListener());
	}
	
	private class DockWindowListener implements IDEDockWindow.Listener {
		@Override
		public void onOpen(IDESourceFile sourceFile) {
			SourceEditor editor = new SourceEditor(DStyleClass.EMPTY, window, sourceFile);
			TabbedViewComponent tab = new TabbedViewComponent(
					sourceFile.getName(),
					null,
					new DScrollPane(DStyleClass.inline(new DStylesheetBuilder()
							.border("border", context -> new DLineBorder(0xFF888888, 1))
							//.shadow("shadow", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 3 * context.getScale()))
							.build()), editor, new SimpleLiveInt(0)),
					editor.isUpdated());
			tabs.tabs.add(tab);
			tabs.currentTab.setValue(tab);
		}
	}
}
