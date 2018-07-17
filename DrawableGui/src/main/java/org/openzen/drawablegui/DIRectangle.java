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
	
	public boolean overlaps(DIRectangle other) {
		if (x + width < other.x)
			return false;
		if (y + height < other.y)
			return false;
		if (other.x + other.width < x)
			return false;
		if (other.y + other.height < y)
			return false;
		
		return true;
	}
	
	public DIRectangle offset(int x, int y) {
		return new DIRectangle(this.x + x, this.y + y, width, height);
	}
	
	public DIRectangle expand(int edge) {
		return new DIRectangle(x - edge, y - edge, width + 2 * edge, height + 2 * edge);
	}
	
	public DIRectangle union(DIRectangle other) {
		int left = Math.min(x, other.x);
		int top = Math.min(y, other.y);
		int right = Math.max(x + width, other.x + other.width);
		int bottom = Math.max(y + height, other.y + other.height);
		return new DIRectangle(left, top, right - left, bottom - top);
	}
	
	public DIRectangle intersect(DIRectangle other) {
		int left = Math.max(x, other.x);
		int top = Math.max(y, other.y);
		int right = Math.min(x + width, other.x + other.width);
		int bottom = Math.min(y + height, other.y + other.height);
		if (right < left || bottom < top)
			return EMPTY;
		
		return new DIRectangle(left, top, right - left, bottom - top);
	}
	
	@Override
	public String toString() {
		return "(x = " + x + ", y = " + y + ", width = " + width + ", height = " + height + ")";
	}
}
