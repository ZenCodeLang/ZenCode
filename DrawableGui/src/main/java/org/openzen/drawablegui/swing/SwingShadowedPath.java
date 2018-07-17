/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DPathBoundsCalculator;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.style.DShadow;
import static org.openzen.drawablegui.swing.SwingCanvas.getGaussianBlurFilter;
import static org.openzen.drawablegui.swing.SwingCanvas.getTransform;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingShadowedPath extends SwingDrawnElement implements DDrawnShape {
	private final DPath originalPath;
	private final GeneralPath path;
	private final BufferedImage shadowImage;
	private final DIRectangle shadowBounds;
	private final int shadowOffset;
	
	private Color awtColor;
	private AffineTransform transform;
	private DIRectangle bounds;
	
	public SwingShadowedPath(
			SwingDrawSurface target,
			int z,
			DTransform2D transform,
			DPath originalPath,
			GeneralPath path,
			int color,
			DShadow shadow) {
		super(target, z);
		
		this.transform = getTransform(transform);
		this.originalPath = originalPath;
		this.path = path;
		this.awtColor = new Color(color, true);
		
		shadowBounds = DPathBoundsCalculator.getBounds(originalPath, transform.offset(shadow.offsetX, shadow.offsetY));
		shadowOffset = 2 * (int)Math.ceil(shadow.radius);
		
		BufferedImage image = new BufferedImage(shadowBounds.width + 2 * shadowOffset, shadowBounds.height + 2 * shadowOffset, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D imageG = (Graphics2D) image.getGraphics();
		
		imageG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		imageG.setColor(new Color(shadow.color, true));
		imageG.setTransform(getTransform(transform.offset(shadowOffset + shadow.offsetX - shadowBounds.x, shadowOffset + shadow.offsetY - shadowBounds.y)));
		imageG.fill(path);
		
		image = getGaussianBlurFilter((int)Math.ceil(shadow.radius), true).filter(image, null);
		image = getGaussianBlurFilter((int)Math.ceil(shadow.radius), false).filter(image, null);
		shadowImage = image;
		
		bounds = new DIRectangle(
				shadowBounds.x - shadowOffset,
				shadowBounds.y - shadowOffset,
				shadowBounds.width + 2 * shadowOffset,
				shadowBounds.height + 2 * shadowOffset);
	}

	@Override
	public void setTransform(DTransform2D transform) {
		this.transform = getTransform(transform);
	}
	
	@Override
	public DIRectangle getBounds() {
		return bounds;
	}

	@Override
	public void setColor(int color) {
		this.awtColor = new Color(color, true);
		invalidate();
	}

	@Override
	public void paint(Graphics2D g, DIRectangle clip) {
		g.drawImage(shadowImage, shadowBounds.x - shadowOffset, shadowBounds.y - shadowOffset, null);
		
		AffineTransform old = g.getTransform();
		g.setColor(awtColor);
		g.transform(transform);
		g.fill(path);
		g.setTransform(old);
	}

	@Override
	public void close() {
		target.remove(this);
	}
}
