/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.draw.DDrawnRectangle;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingDrawnRectangle extends SwingDrawnElement implements DDrawnRectangle {
	private DIRectangle rectangle;
	private Color color;
	
	public SwingDrawnRectangle(SwingDrawSurface target, int z, DIRectangle rectangle, int color) {
		super(target, z);
		
		this.rectangle = rectangle;
		this.color = new Color(color, true);
	}

	@Override
	public void setRectangle(DIRectangle rectangle) {
		this.rectangle = rectangle;
	}

	@Override
	public void setColor(int color) {
		this.color = new Color(color, true);
	}

	@Override
	public void paint(Graphics2D g) {
		g.setColor(color);
		g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}
}
