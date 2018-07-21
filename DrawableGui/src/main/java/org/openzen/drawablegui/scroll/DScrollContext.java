/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.scroll;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DScrollContext {
	void scrollInView(int x, int y, int width, int height);
	
	int getViewportWidth();
	
	int getViewportHeight();
}
