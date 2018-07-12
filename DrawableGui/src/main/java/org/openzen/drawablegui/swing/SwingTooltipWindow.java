/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import javax.swing.JWindow;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DTooltipHandle;
import org.openzen.drawablegui.style.DStylePathRoot;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingTooltipWindow extends JWindow implements DTooltipHandle {
	private final SwingGraphicsContext context;
	
	public SwingTooltipWindow(SwingGraphicsContext parent, DComponent rootComponent) {
		SwingRoot root = new SwingRoot(rootComponent);
		context = new SwingGraphicsContext(parent.getStylesheets(), parent.getScale(), parent.getTextScale(), root);
		rootComponent.setContext(DStylePathRoot.INSTANCE, context);
		rootPane.add(root);
		pack();
	}
	
	@Override
	public void close() {
		dispose();
	}
}
