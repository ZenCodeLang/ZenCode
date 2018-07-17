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
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DPathBoundsCalculator;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawnShape;
import static org.openzen.drawablegui.swing.SwingCanvas.getTransform;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingFilledPath extends SwingDrawnElement implements DDrawnShape {
	private Color awtColor;
	private AffineTransform transform;
	
	private final DPath originalPath;
	private final GeneralPath path;
	private DIRectangle bounds;
	
	public SwingFilledPath(
			SwingDrawSurface target,
			int z,
			DTransform2D transform,
			DPath originalPath,
			GeneralPath path,
			int color) {
		super(target, z);
		
		this.originalPath = originalPath;
		this.path = path;
		
		this.awtColor = color == 0 ? null : new Color(color, true);
		this.transform = getTransform(transform);
		this.bounds = DPathBoundsCalculator.getBounds(originalPath, transform);
	}

	@Override
	public void setTransform(DTransform2D transform) {
		this.transform = getTransform(transform);
		this.bounds = DPathBoundsCalculator.getBounds(originalPath, transform);
	}
	
	@Override
	public DIRectangle getBounds() {
		return bounds;
	}

	@Override
	public void setColor(int color) {
		this.awtColor = color == 0 ? null : new Color(color, true);
		invalidate();
	}

	@Override
	public void paint(Graphics2D g, DIRectangle clip) {
		if (awtColor == null)
			return;
		
		AffineTransform old = g.getTransform();
		g.setColor(awtColor);
		g.transform(transform);
		g.fill(path);
		g.setTransform(old);
	}
}
