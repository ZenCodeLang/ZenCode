/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.listeners.DIRectangle;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DCanvas {
	void pushBounds(DIRectangle bounds);
	
	void popBounds();
	
	void pushOffset(int x, int y);
	
	void popOffset();
	
	DIRectangle getBounds();
	
	DUIContext getContext();
	
	void drawText(DFont font, int color, float x, float y, String text);
	
	/**
	 * Strokes a given path.
	 * 
	 * @param path path to be stroked
	 * @param transform path transform
	 * @param color path color
	 * @param lineWidth path line width
	 */
	void strokePath(DPath path, DTransform2D transform, int color, float lineWidth);
	
	/**
	 * Fills a given path.
	 * 
	 * @param path path to be filled
	 * @param transform 
	 * @param color
	 */
	void fillPath(DPath path, DTransform2D transform, int color);
	
	/**
	 * Draws the shadow for a given path.
	 * 
	 * @param path
	 * @param transform
	 * @param color
	 * @param dx
	 * @param dy
	 * @param radius 
	 */
	void shadowPath(DPath path, DTransform2D transform, int color, float dx, float dy, float radius);
	
	/**
	 * Fills a rectangle.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param color 
	 */
	void fillRectangle(int x, int y, int width, int height, int color);
}
