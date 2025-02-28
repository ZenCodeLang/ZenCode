/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.draw.DDrawTarget;
import org.openzen.drawablegui.draw.DDrawnColorableElement;
import org.openzen.drawablegui.draw.DDrawnElement;
import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.style.DShadow;

/**
 * @author Hoofdgebruiker
 */
public class DDrawableInstance implements Closeable {
	private final List<DDrawnElement> elements = new ArrayList<>();

	public DDrawableInstance(DDrawSurface surface, int z, DDrawable icon, DTransform2D transform) {
		icon.draw(new DrawTarget(surface), z, transform);
	}

	public void setTransform(DTransform2D transform) {
		for (DDrawnElement element : elements)
			element.setTransform(transform);
	}

	@Override
	public void close() {
		for (DDrawnElement element : elements)
			element.close();
	}

	private class DrawTarget implements DDrawTarget {
		private final DDrawSurface surface;

		public DrawTarget(DDrawSurface surface) {
			this.surface = surface;
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
			return addElement(surface.drawText(z, font, color, x, y, text));
		}

		@Override
		public DDrawnRectangle fillRect(int z, DIRectangle rectangle, int color) {
			return addElement(surface.fillRect(z, rectangle, color));
		}

		@Override
		public DDrawnShape strokePath(int z, DPath path, DTransform2D transform, int color, float lineWidth) {
			return addElement(surface.strokePath(z, path, transform, color, lineWidth));
		}

		@Override
		public DDrawnShape fillPath(int z, DPath path, DTransform2D transform, int color) {
			return addElement(surface.fillPath(z, path, transform, color));
		}

		@Override
		public DDrawnShape shadowPath(int z, DPath path, DTransform2D transform, int color, DShadow shadow) {
			return addElement(surface.shadowPath(z, path, transform, color, shadow));
		}

		private <T extends DDrawnColorableElement> T addElement(T colorable) {
			elements.add(colorable);
			return colorable;
		}
	}
}
