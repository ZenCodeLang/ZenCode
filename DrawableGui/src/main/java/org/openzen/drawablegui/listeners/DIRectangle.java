/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.listeners;

/**
 *
 * @author Hoofdgebruiker
 */
public class DIRectangle {
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	
	public DIRectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean contains(int x, int y) {
		return x >= this.x && x < (this.x + this.width)
				&& y >= this.y && y < (this.y + this.height);
	}
}
