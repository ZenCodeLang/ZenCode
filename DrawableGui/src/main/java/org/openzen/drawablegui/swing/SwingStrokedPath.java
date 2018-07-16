/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawnShape;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingStrokedPath extends SwingDrawnElement implements DDrawnShape {
	private DTransform2D transform;
	private int color;
	
	private final GeneralPath path;
	private final float lineWidth;
	
	public SwingStrokedPath(
			SwingDrawSurface target,
			int z,
			DTransform2D transform,
			int color,
			GeneralPath path,
			float lineWidth) {
		super(target, z);
		
		this.transform = transform;
		this.color = color;
		this.path = path;
		this.lineWidth = lineWidth;
	}

	@Override
	public void setTransform(DTransform2D transform) {
		this.transform = transform;
	}

	@Override
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public void close() {
		target.remove(this);
	}

	@Override
	public void paint(Graphics2D g) {
		if (color == 0)
			return;
		
		AffineTransform old = g.getTransform();
		g.setColor(new Color(color, true));
		g.setStroke(new BasicStroke(lineWidth));
		g.transform(SwingCanvas.getTransform(transform));
		g.draw(path);
		g.setTransform(old);
	}
}
