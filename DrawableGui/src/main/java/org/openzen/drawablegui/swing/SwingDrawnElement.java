/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Graphics2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.draw.DDrawnElement;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class SwingDrawnElement implements DDrawnElement {
	public final SwingDrawSurface target;
	public final int z;
	
	public SwingDrawnElement(SwingDrawSurface target, int z) {
		this.target = target;
		this.z = z;
	}
	
	abstract void paint(Graphics2D g, DIRectangle clip);
	
	@Override
	public void close() {
		target.remove(this);
	}
	
	protected void invalidate() {
		target.repaint(getBounds());
	}
}
