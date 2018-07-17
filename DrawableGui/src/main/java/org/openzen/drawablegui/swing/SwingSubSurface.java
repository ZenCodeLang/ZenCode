/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.draw.DSubSurface;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleDefinition;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingSubSurface extends SwingDrawnElement implements DSubSurface {
	private final SwingDrawSurface surface;
	
	private DIRectangle clip = null;
	
	public SwingSubSurface(SwingDrawSurface parent, int z) {
		super(parent, z);
		
		this.surface = new SwingDrawSurface(parent.getContext(), 0, 0);
	}
	
	@Override
	public void paint(Graphics2D g, DIRectangle clip) {
		Rectangle clipBounds = g.getClipBounds();
		if (this.clip != null) {
			g.setClip(this.clip.x, this.clip.y, this.clip.width, this.clip.height);
		}
		
		AffineTransform oldTransform = g.getTransform();
		g.transform(AffineTransform.getTranslateInstance(surface.offsetX, surface.offsetY));
		DIRectangle newClip = this.clip == null ? clip : clip.offset(-surface.offsetX, -surface.offsetY).intersect(this.clip.offset(-surface.offsetX, -surface.offsetY));
		surface.paint(g, newClip);
		g.setTransform(oldTransform);
		g.setClip(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
	}
	
	@Override
	public void setTransform(DTransform2D transform) {
		setOffset((int)transform.xx, (int)transform.yy);
	}

	@Override
	public void setOffset(int x, int y) {
		surface.setOffset(x, y);
		repaint();
	}

	@Override
	public void setClip(DIRectangle bounds) {
		clip = bounds;
	}

	@Override
	public DUIContext getContext() {
		return surface.getContext();
	}
	
	@Override
	public DIRectangle getBounds() {
		return surface.calculateBounds();
	}

	@Override
	public DStyleDefinition getStylesheet(DStylePath path) {
		return surface.getStylesheet(path);
	}

	@Override
	public DFontMetrics getFontMetrics(DFont font) {
		return surface.getFontMetrics(font);
	}

	@Override
	public float getScale() {
		return surface.getScale();
	}

	@Override
	public float getTextScale() {
		return surface.getTextScale();
	}

	@Override
	public DDrawnText drawText(int z, DFont font, int color, float x, float y, String text) {
		return surface.drawText(z, font, color, x, y, text);
	}

	@Override
	public DDrawnShape strokePath(int z, DPath path, DTransform2D transform, int color, float lineWidth) {
		return surface.strokePath(z, path, transform, color, lineWidth);
	}
	
	@Override
	public DDrawnRectangle fillRect(int z, DIRectangle rectangle, int color) {
		return surface.fillRect(z, rectangle, color);
	}

	@Override
	public DDrawnShape fillPath(int z, DPath path, DTransform2D transform, int color) {
		return surface.fillPath(z, path, transform, color);
	}

	@Override
	public DDrawnShape shadowPath(int z, DPath path, DTransform2D transform, int color, DShadow shadow) {
		return surface.shadowPath(z, path, transform, color, shadow);
	}

	@Override
	public DSubSurface createSubSurface(int z) {
		return surface.createSubSurface(z);
	}

	@Override
	public void repaint(int x, int y, int width, int height) {
		int left = toGlobalX(x);
		int top = toGlobalY(y);
		int right = left + width;
		int bottom = top + height;

		if (clip != null) {
			left = Math.max(clip.x, left);
			top = Math.max(clip.y, top);
			right = Math.min(clip.x + clip.width, right);
			bottom = Math.min(clip.y + clip.height, bottom);
		}

		if (left >= right || top >= bottom)
			return;

		target.repaint(left, top, right - left, bottom - top);
	}
	
	@Override
	public void repaint() {
		DIRectangle bounds = surface.calculateBounds().offset(surface.offsetX, surface.offsetY);
		if (clip != null)
			bounds = bounds.intersect(clip);
		
		target.repaint(bounds);
	}

	private int toGlobalX(int x) {
		return x + surface.offsetX;
	}

	private int toGlobalY(int y) {
		return y + surface.offsetY;
	}

	private int toLocalX(int x) {
		return x - surface.offsetX;
	}

	private int toLocalY(int y) {
		return y - surface.offsetY;
	}
}
