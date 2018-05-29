/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.live.LiveObject;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DComponent {
	void setContext(DDrawingContext context);
	
	LiveObject<DDimensionPreferences> getDimensionPreferences();
	
	DRectangle getBounds();
	
	void setBounds(DRectangle bounds);
	
	void paint(DCanvas canvas);
	
	default void onMouseEnter(DMouseEvent e) {}
	
	default void onMouseExit(DMouseEvent e) {}
	
	default void onMouseMove(DMouseEvent e) {}
	
	default void onMouseDrag(DMouseEvent e) {}
	
	default void onMouseClick(DMouseEvent e) {}
	
	default void onMouseDown(DMouseEvent e) {}
	
	default void onMouseRelease(DMouseEvent e) {}
	
	default void onMouseScroll(DMouseEvent e) {}
	
	default void onFocusLost() {}
	
	default void onFocusGained() {}
	
	default void onKeyTyped(DKeyEvent e) {}
	
	default void onKeyPressed(DKeyEvent e) {}
	
	default void onKeyReleased(DKeyEvent e) {}
}
