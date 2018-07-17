/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.border;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.draw.DDrawSurface;

/**
 *
 * @author Hoofdgebruiker
 */
public class DPaddedBorder implements DBorder {
	private final int left;
	private final int top;
	private final int right;
	private final int bottom;
	
	public DPaddedBorder(int left, int top, int right, int bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public DPaddedBorder(int size) {
		this(size, size, size, size);
	}

	@Override
	public void update(DDrawSurface surface, int z, DIRectangle bounds) {
		// nothing to paint
	}

	@Override
	public int getPaddingLeft() {
		return left;
	}

	@Override
	public int getPaddingRight() {
		return right;
	}

	@Override
	public int getPaddingTop() {
		return top;
	}

	@Override
	public int getPaddingBottom() {
		return bottom;
	}

	@Override
	public void close() {
		
	}
}
