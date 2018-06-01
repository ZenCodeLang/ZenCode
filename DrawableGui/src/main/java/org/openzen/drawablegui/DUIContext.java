/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.listeners.DIRectangle;
import org.openzen.drawablegui.style.DStyleSheets;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DUIContext {
	DStyleSheets getStylesheets();
	
	float getScale();
	
	void repaint(int x, int y, int width, int height);
	
	default void repaint(DIRectangle rectangle) {
		repaint(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}
	
	void setCursor(Cursor cursor);
	
	void focus(DComponent component);
	
	void scrollInView(int x, int y, int width, int height);
	
	DTimerHandle setTimer(int millis, Runnable target);
	
	DClipboard getClipboard();
	
	DFontMetrics getFontMetrics(DFont font);
	
	enum Cursor {
		NORMAL,
		HAND,
		MOVE,
		TEXT,
		E_RESIZE,
		S_RESIZE,
		NE_RESIZE,
		NW_RESIZE,
	}
}
