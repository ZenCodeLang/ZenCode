/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawnRectangle;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingDrawnRectangle extends SwingDrawnElement implements DDrawnRectangle {
	private DIRectangle rectangle;
	private Color awtColor;
	
	public SwingDrawnRectangle(SwingDrawSurface target, int z, DIRectangle rectangle, int color) {
		super(target, z);
		
		this.rectangle = rectangle;
		this.awtColor = new Color(color, true);
	}
	
	@Override
	public void setTransform(DTransform2D transform) {
		// not supported?
	}
	
	@Override
	public DIRectangle getBounds() {
		return rectangle;
	}

	@Override
	public void setRectangle(DIRectangle rectangle) {
		invalidate();
		this.rectangle = rectangle;
		invalidate();
	}

	@Override
	public void setColor(int color) {
		this.awtColor = new Color(color, true);
		invalidate();
	}

	@Override
	public void paint(Graphics2D g, DIRectangle clip) {
		g.setColor(awtColor);
		g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
		//g.fillRect(clip.x, clip.y, 3, 3);
	}
}
