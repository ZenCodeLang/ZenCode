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
import org.openzen.drawablegui.style.DShadow;

/**
 * @author Hoofdgebruiker
 */
public interface DDrawTarget {
	public static final int INSTANCE_COLOR = 1;

	DFontMetrics getFontMetrics(DFont font);

	float getScale();

	float getTextScale();

	DDrawnText drawText(int z, DFont font, int color, float x, float y, String text);

	DDrawnRectangle fillRect(int z, DIRectangle rectangle, int color);

	DDrawnShape strokePath(int z, DPath path, DTransform2D transform, int color, float lineWidth);

	DDrawnShape fillPath(int z, DPath path, DTransform2D transform, int color);

	DDrawnShape shadowPath(int z, DPath path, DTransform2D transform, int color, DShadow shadow);
}
