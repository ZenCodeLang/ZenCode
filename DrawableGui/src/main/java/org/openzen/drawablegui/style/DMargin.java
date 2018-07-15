/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

/**
 *
 * @author Hoofdgebruiker
 */
public class DMargin {
	public static final DMargin EMPTY = new DMargin(0);
	public static final DMarginElement EMPTY_ELEMENT = context -> EMPTY;
	
	public final int left;
	public final int right;
	public final int top;
	public final int bottom;
	
	public DMargin(int left, int right, int top, int bottom) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}
	
	public DMargin(int amount) {
		this(amount, amount, amount, amount);
	}
	
	public int getVertical() {
		return top + bottom;
	}
	
	public int getHorizontal() {
		return left + right;
	}
}
