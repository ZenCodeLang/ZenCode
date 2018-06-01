/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DUIContext;

/**
 *
 * @author Hoofdgebruiker
 */
public final class SwingWindow extends JFrame {
	private final SwingRoot swingComponent;
	
	public SwingWindow(String title, DComponent root) {
		super(title);
		
		getContentPane().add(swingComponent = new SwingRoot(root), BorderLayout.CENTER);
		swingComponent.requestFocusInWindow();
	}
	
	public DUIContext getContext() {
		return swingComponent.context;
	}
}
