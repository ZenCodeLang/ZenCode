/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import live.LiveObject;
import live.LiveString;
import live.SimpleLiveObject;
import live.SimpleLiveString;

import org.openzen.drawablegui.DEmptyView;
import org.openzen.drawablegui.DScalableSize;
import org.openzen.drawablegui.border.DLineBorder;
import org.openzen.drawablegui.scroll.DScrollPane;
import org.openzen.drawablegui.layout.DSideLayout;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylesheetBuilder;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.IDECompileState;
import org.openzen.zenscript.ide.host.IDEPropertyDirectory;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.host.IDETarget;
import org.openzen.zenscript.ide.ui.IDEDockWindow;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.view.aspectbar.AspectBarView;
import org.openzen.zenscript.ide.ui.view.editor.SourceEditor;
import org.openzen.zenscript.ide.ui.view.output.OutputView;
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
	
	private final LiveObject<IDECompileState> compileState;
	
	public WindowView(IDEWindow window, DevelopmentHost host, IDEPropertyDirectory settings, LiveObject<IDECompileState> compileState) {
		super(DStyleClass.inline(
				new DStylesheetBuilder().color("backgroundColor", 0xFFEEEEEE).build()),
				DEmptyView.INSTANCE);
		this.window = window;
		this.compileState = compileState;
		
		projectBrowser = new ProjectBrowser(window, host, settings.getSubdirectory("projectBrowserExpansionState"));
		
		OutputView output = new OutputView(DStyleClass.EMPTY, window.output);
		
		DScalableSize outputSize = new DScalableSize(new DDpDimension(250), new DDpDimension(250));
		DScrollPane scrollOutput = new DScrollPane(DStyleClass.inline(new DStylesheetBuilder()
							.marginDp("margin", 3)
							.color("backgroundColor", 0xFFFFFFFF)
							.shadow("shadow", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 3 * context.getScale()))
							.build()), output, new SimpleLiveObject<>(outputSize));
		
		setMain(tabs = new TabbedView(DStyleClass.inline(new DStylesheetBuilder()
				.marginDp("margin", 3)
				.build())));
		add(Side.BOTTOM, scrollOutput);
		add(Side.LEFT, projectBrowser.view);
		add(Side.BOTTOM, new StatusBarView(DStyleClass.EMPTY, status));
		add(Side.TOP, new AspectBarView(DStyleClass.EMPTY, window.aspectBar));
		
		window.dockWindow.addListener(new DockWindowListener());
	}
	
	private class DockWindowListener implements IDEDockWindow.Listener {
		@Override
		public void onOpen(IDESourceFile sourceFile) {
			SourceEditor editor = new SourceEditor(DStyleClass.EMPTY, window, sourceFile, compileState);
			DScalableSize size = new DScalableSize(new DDpDimension(280), new DDpDimension(280));
			TabbedViewComponent tab = new TabbedViewComponent(
					sourceFile.getName(),
					null,
					new DScrollPane(DStyleClass.inline(new DStylesheetBuilder()
							.border("border", context -> new DLineBorder(0xFF888888, 1))
							//.shadow("shadow", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 3 * context.getScale()))
							.build()), editor, new SimpleLiveObject<>(size)),
					editor.isUpdated());
			tabs.tabs.add(tab);
			tabs.currentTab.setValue(tab);
		}
	}
}
