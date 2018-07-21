/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import java.util.function.Function;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.draw.DSubSurface;
import org.openzen.drawablegui.scroll.DScrollContext;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStyleDefinition;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DComponentContext {
	public final DScrollContext scrollContext;
	public final DStylePath path;
	public final int z;
	public final DDrawSurface surface;
	
	public DComponentContext(DScrollContext scrollContext, DStylePath path, int z, DDrawSurface surface) {
		this.scrollContext = scrollContext;
		this.path = path;
		this.z = z;
		this.surface = surface;
	}
	
	public DComponentContext getChildContext(String component, DStyleClass styleClass) {
		return getChildContext(10, component, styleClass);
	}
	
	public DComponentContext getChildContext(int deltaz, String component, DStyleClass styleClass) {
		return new DComponentContext(scrollContext, path.getChild(component, styleClass), z + deltaz, surface);
	}
	
	public DUIContext getUIContext() {
		return surface.getContext();
	}
	
	public DStyleDefinition getStyle() {
		return surface.getStylesheet(path);
	}
	
	public <S> S getStyle(Function<DStyleDefinition, S> factory) {
		return factory.apply(surface.getStylesheet(path));
	}

	public DFontMetrics getFontMetrics(DFont font) {
		return surface.getFontMetrics(font);
	}
	
	public float getScale() {
		return surface.getScale();
	}
	
	public float getTextScale() {
		return surface.getTextScale();
	}
	
	public DDrawnText drawText(int deltaz, DFont font, int color, float x, float y, String text) {
		return surface.drawText(z + deltaz, font, color, x, y, text);
	}
	
	public DDrawnRectangle fillRect(int deltaz, DIRectangle rectangle, int color) {
		return surface.fillRect(z + deltaz, rectangle, color);
	}
	
	public DDrawnShape strokePath(int deltaz, DPath path, DTransform2D transform, int color, float lineWidth) {
		return surface.strokePath(z + deltaz, path, transform, color, lineWidth);
	}
	
	public DDrawnShape fillPath(int deltaz, DPath path, DTransform2D transform, int color) {
		return surface.fillPath(z + deltaz, path, transform, color);
	}
	
	public DDrawnShape shadowPath(int deltaz, DPath path, DTransform2D transform, int color, DShadow shadow) {
		return surface.shadowPath(z + deltaz, path, transform, color, shadow);
	}
	
	public DSubSurface createSubSurface(int deltaz) {
		return surface.createSubSurface(z + deltaz);
	}
}
