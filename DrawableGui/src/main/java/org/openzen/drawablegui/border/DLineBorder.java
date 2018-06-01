/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.border;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.listeners.DIRectangle;
import org.openzen.drawablegui.DUIContext;

/**
 *
 * @author Hoofdgebruiker
 */
public class DLineBorder implements DBorder {
	private final int color;
	private final int borderWidth;
	
	public DLineBorder(int color, int borderWidth) {
		this.color = color;
		this.borderWidth = borderWidth;
	}

	@Override
	public void paint(DCanvas canvas, DIRectangle bounds) {
		canvas.strokePath(tracer -> {
				tracer.moveTo(bounds.x, bounds.y);
				tracer.lineTo(bounds.x + bounds.width - borderWidth, bounds.y);
				tracer.lineTo(bounds.x + bounds.width - borderWidth, bounds.y + bounds.height - borderWidth);
				tracer.lineTo(bounds.x, bounds.y + bounds.height - borderWidth);
				tracer.close();
			}, DTransform2D.IDENTITY, color, borderWidth);
	}

	@Override
	public int getPaddingLeft() {
		return borderWidth;
	}

	@Override
	public int getPaddingRight() {
		return borderWidth;
	}

	@Override
	public int getPaddingTop() {
		return borderWidth;
	}

	@Override
	public int getPaddingBottom() {
		return borderWidth;
	}
}
