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
 *
 * @author Hoofdgebruiker
 */
public final class DColorableIconInstance implements Closeable {
	private final List<DDrawnElement> elements = new ArrayList<>();
	private final List<DDrawnColorableElement> colorables = new ArrayList<>();
	
	public DColorableIconInstance(DDrawSurface surface, int z, DColorableIcon icon, DTransform2D transform, int color) {
		icon.draw(new DrawTarget(surface), z, transform, DDrawTarget.INSTANCE_COLOR);
		setColor(color);
	}
	
	public void setColor(int color) {
		for (DDrawnColorableElement colorable : colorables)
			colorable.setColor(color);
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
			return addColorable(surface.drawText(z, font, color, x, y, text), color);
		}

		@Override
		public DDrawnRectangle fillRect(int z, DIRectangle rectangle, int color) {
			return addColorable(surface.fillRect(z, rectangle, color), color);
		}

		@Override
		public DDrawnShape strokePath(int z, DPath path, DTransform2D transform, int color, float lineWidth) {
			return addColorable(surface.strokePath(z, path, transform, color, lineWidth), color);
		}

		@Override
		public DDrawnShape fillPath(int z, DPath path, DTransform2D transform, int color) {
			return addColorable(surface.fillPath(z, path, transform, color), color);
		}

		@Override
		public DDrawnShape shadowPath(int z, DPath path, DTransform2D transform, int color, DShadow shadow) {
			return addColorable(surface.shadowPath(z, path, transform, color, shadow), color);
		}
		
		private <T extends DDrawnColorableElement> T addColorable(T colorable, int color) {
			if (color == DDrawTarget.INSTANCE_COLOR)
				colorables.add(colorable);
			
			elements.add(colorable);
			return colorable;
		}
	}
}
