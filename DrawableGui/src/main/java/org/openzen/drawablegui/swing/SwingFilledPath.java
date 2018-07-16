/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawnShape;
import static org.openzen.drawablegui.swing.SwingCanvas.getTransform;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingFilledPath extends SwingDrawnElement implements DDrawnShape {
	private int color;
	private DTransform2D transform;
	
	private final GeneralPath path;
	
	public SwingFilledPath(
			SwingDrawSurface target,
			int z,
			DTransform2D transform,
			GeneralPath path,
			int color) {
		super(target, z);
		
		this.path = path;
		
		this.color = color;
		this.transform = transform;
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
	public void paint(Graphics2D g) {
		if (color == 0)
			return;
		
		AffineTransform old = g.getTransform();
		g.setColor(new Color(color, true));
		g.transform(getTransform(transform));
		g.fill(path);
		g.setTransform(old);
	}
}
