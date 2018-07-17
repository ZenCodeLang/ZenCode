/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.border;

import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.draw.DDrawSurface;

/**
 *
 * @author Hoofdgebruiker
 */
public class DCompositeBorder implements DBorder {
	private final DBorder[] borders;
	
	public DCompositeBorder(DBorder... borders) {
		this.borders = borders;
	}

	@Override
	public void update(DDrawSurface surface, int z, DIRectangle bounds) {
		for (DBorder border : borders) {
			border.update(surface, z, bounds);
			bounds = new DIRectangle(
					bounds.x + border.getPaddingLeft(),
					bounds.y + border.getPaddingTop(),
					bounds.width - border.getPaddingLeft() - border.getPaddingRight(),
					bounds.height - border.getPaddingTop() - border.getPaddingBottom());
		}
	}

	@Override
	public int getPaddingLeft() {
		int total = 0;
		for (DBorder border : borders)
			total += border.getPaddingLeft();
		return total;
	}

	@Override
	public int getPaddingRight() {
		int total = 0;
		for (DBorder border : borders)
			total += border.getPaddingRight();
		return total;
	}

	@Override
	public int getPaddingTop() {
		int total = 0;
		for (DBorder border : borders)
			total += border.getPaddingTop();
		return total;
	}

	@Override
	public int getPaddingBottom() {
		int total = 0;
		for (DBorder border : borders)
			total += border.getPaddingBottom();
		return total;
	}
	
	@Override
	public void close() {
		for (DBorder border : borders)
			border.close();
	}
}
