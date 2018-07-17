/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import java.io.Closeable;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DComponent extends Closeable {
	void mount(DStylePath parent, int z, DDrawSurface surface);
	
	void unmount();
	
	LiveObject<DSizing> getSizing();
	
	DIRectangle getBounds();
	
	int getBaselineY();
	
	void setBounds(DIRectangle bounds);
	
	default void onMounted() {}
	
	default void onUnmounted() {}
	
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
	
	@Override
	void close();
}
