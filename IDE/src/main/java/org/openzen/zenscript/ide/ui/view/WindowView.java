/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DEmptyView;
import org.openzen.drawablegui.scroll.DScrollPane;
import org.openzen.drawablegui.scroll.DScrollPaneStyle;
import org.openzen.drawablegui.DSideLayout;
import org.openzen.drawablegui.tree.DTreeView;
import org.openzen.drawablegui.tree.DTreeViewStyle;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.ide.ui.view.editor.SourceEditor;
import org.openzen.zenscript.ide.ui.view.project.RootTreeNode;

/**
 *
 * @author Hoofdgebruiker
 */
public final class WindowView extends DSideLayout {
	public WindowView(IDEWindow window, DevelopmentHost host) {
		super(DEmptyView.INSTANCE);
		
		DTreeView projectView = new DTreeView(DTreeViewStyle.DEFAULT, new RootTreeNode(window, host), false);
		projectView.getDimensionPreferences().setValue(new DDimensionPreferences(500, 500));
		add(Side.LEFT, new DScrollPane(DScrollPaneStyle.DEFAULT, projectView));
		add(Side.BOTTOM, new StatusBarView());
		
		window.dockWindow.currentSourceFile.addListener((oldSource, newSource) -> {
			if (newSource == null) {
				setMain(DEmptyView.INSTANCE);
			} else {
				setMain(new DScrollPane(DScrollPaneStyle.DEFAULT, new SourceEditor(newSource)));
			}
		});
	}
}
