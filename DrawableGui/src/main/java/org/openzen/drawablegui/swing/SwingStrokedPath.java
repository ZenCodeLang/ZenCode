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
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DPathBoundsCalculator;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawnShape;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingStrokedPath extends SwingDrawnElement implements DDrawnShape {
	private AffineTransform transform;
	private Color awtColor;
	
	private final DPath originalPath;
	private final GeneralPath path;
	private final BasicStroke stroke;
	private DIRectangle bounds;
	
	public SwingStrokedPath(
			SwingDrawSurface target,
			int z,
			DTransform2D transform,
			int color,
			DPath originalPath,
			GeneralPath path,
			float lineWidth) {
		super(target, z);
		
		this.transform = SwingDrawSurface.getTransform(transform);
		this.awtColor = color == 0 ? null : new Color(color, true);
		this.originalPath = originalPath;
		this.path = path;
		this.stroke = new BasicStroke(lineWidth);
		this.bounds = DPathBoundsCalculator.getBounds(originalPath, transform).expand((int)(lineWidth + 0.5f));
	}

	@Override
	public void setTransform(DTransform2D transform) {
		invalidate();
		this.transform = SwingDrawSurface.getTransform(transform);
		this.bounds = DPathBoundsCalculator.getBounds(originalPath, transform);
		invalidate();
	}

	@Override
	public DIRectangle getBounds() {
		return bounds;
	}
	
	@Override
	public void setColor(int color) {
		awtColor = color == 0 ? null : new Color(color, true);
		invalidate();
	}

	@Override
	public void paint(Graphics2D g, DIRectangle clip) {
		if (awtColor == null)
			return;
		
		AffineTransform old = g.getTransform();
		g.setColor(awtColor);
		g.setStroke(stroke);
		g.transform(transform);
		g.draw(path);
		g.setTransform(old);
	}
}
