/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.draw.DDrawSurface;
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
public class SwingDrawSurface implements DDrawSurface {
	private final List<SwingDrawnElement> elements = new ArrayList<>();
	private final SwingGraphicsContext context;
	
	public SwingDrawSurface(SwingGraphicsContext context) {
		this.context = context;
	}
	
	public void paint(Graphics2D g) {
		for (SwingDrawnElement element : elements)
			element.paint(g);
	}

	@Override
	public SwingGraphicsContext getContext() {
		return context;
	}

	@Override
	public DStyleDefinition getStylesheet(DStylePath path) {
		return context.getStylesheets().get(context, path);
	}
	
	@Override
	public DFontMetrics getFontMetrics(DFont font) {
		return context.getFontMetrics(font);
	}
	
	@Override
	public float getScale() {
		return context.getScale();
	}
	
	@Override
	public float getTextScale() {
		return context.getTextScale();
	}

	@Override
	public DDrawnText drawText(int z, DFont font, int color, float x, float y, String text) {
		return addElement(new SwingDrawnText(this, z, x, y, color, font, text));
	}

	@Override
	public DDrawnShape strokePath(int z, DPath path, DTransform2D transform, int color, float lineWidth) {
		return addElement(new SwingStrokedPath(this, z, transform, color, context.getPath(path), lineWidth));
	}
	
	@Override
	public DDrawnRectangle fillRect(int z, DIRectangle rectangle, int color) {
		return addElement(new SwingDrawnRectangle(this, z, rectangle, color));
	}

	@Override
	public DDrawnShape fillPath(int z, DPath path, DTransform2D transform, int color) {
		return addElement(new SwingFilledPath(this, z, transform, context.getPath(path), color));
	}

	@Override
	public DDrawnShape shadowPath(int z, DPath path, DTransform2D transform, int color, DShadow shadow) {
		if (shadow.color == 0 || shadow.radius == 0) {
			return fillPath(z, path, transform, color);
		}
		
		return addElement(new SwingShadowedPath(this, z, transform, path, context.getPath(path), color, shadow));
	}
	
	@Override
	public DSubSurface createSubSurface(int z) {
		return addElement(new SwingSubSurface(this, z));
	}
	
	public void remove(SwingDrawnElement element) {
		elements.remove(element);
	}

	@Override
	public void repaint(int x, int y, int width, int height) {
		context.repaint(x, y, width, height);
	}
	
	private <T extends SwingDrawnElement> T addElement(T element) {
		int index = elements.size();
		while (index > 0 && element.z < elements.get(index - 1).z)
			index--;
		
		elements.add(index, element);
		return element;
	}
}
