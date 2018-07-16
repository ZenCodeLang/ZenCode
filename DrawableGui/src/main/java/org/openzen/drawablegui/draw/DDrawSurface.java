/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.draw;

import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleDefinition;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DDrawSurface {
	DUIContext getContext();
	
	DStyleDefinition getStylesheet(DStylePath path);
	
	DFontMetrics getFontMetrics(DFont font);
	
	float getScale();
	
	float getTextScale();
	
	DDrawnText drawText(int z, DFont font, int color, float x, float y, String text);
	
	DDrawnRectangle fillRect(int z, DIRectangle rectangle, int color);
	
	DDrawnShape strokePath(int z, DPath path, DTransform2D transform, int color, float lineWidth);
	
	DDrawnShape fillPath(int z, DPath path, DTransform2D transform, int color);
	
	DDrawnShape shadowPath(int z, DPath path, DTransform2D transform, int color, DShadow shadow);
	
	DSubSurface createSubSurface(int z);
	
	void repaint(int x, int y, int width, int height);
	
	default void repaint(DIRectangle rectangle) {
		repaint(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}
}
