/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

/**
 *
 * @author Hoofdgebruiker
 */
public class DIRectangle {
	public static final DIRectangle EMPTY = new DIRectangle(0, 0, 0, 0);
	
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	
	public DIRectangle(int x, int y, int width, int height) {
		if (width < 0)
			throw new IllegalArgumentException("Width must be >= 0");
		if (height < 0)
			throw new IllegalArgumentException("Height must be >= 0");
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public int getCenterX() {
		return (x + width) / 2;
	}
	
	public int getCenterY() {
		return (y + height) / 2;
	}
	
	public boolean contains(int x, int y) {
		return x >= this.x && x < (this.x + this.width)
				&& y >= this.y && y < (this.y + this.height);
	}
	
	@Override
	public String toString() {
		return "(x = " + x + ", y = " + y + ", width = " + width + ", height = " + height + ")";
	}
}
