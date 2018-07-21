/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.style.DStyleSheets;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DUIContext {
	DStyleSheets getStylesheets();
	
	float getScale();
	
	float getTextScale();
	
	void repaint(int x, int y, int width, int height);
	
	default void repaint(DIRectangle rectangle) {
		repaint(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}
	
	void setCursor(Cursor cursor);
	
	DTimerHandle setTimer(int millis, Runnable target);
	
	DClipboard getClipboard();
	
	DFontMetrics getFontMetrics(DFont font);
	
	DUIWindow getWindow();
	
	DUIWindow openDialog(int x, int y, DAnchor anchor, String title, DComponent root);
	
	DUIWindow openView(int x, int y, DAnchor anchor, DComponent root);
	
	default int dp(float dp) {
		return (int)(dp * getScale());
	}
	
	default int sp(float sp) {
		return (int)(sp * getTextScale());
	}
	
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
