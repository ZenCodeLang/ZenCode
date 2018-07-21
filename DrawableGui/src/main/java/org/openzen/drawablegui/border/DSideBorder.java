/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.border;

import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawnShape;

/**
 *
 * @author Hoofdgebruiker
 */
public class DSideBorder implements DBorder {
	public final int leftWidth;
	public final int leftColor;
	public final int topWidth;
	public final int topColor;
	public final int rightWidth;
	public final int rightColor;
	public final int bottomWidth;
	public final int bottomColor;
	
	private DDrawnShape left;
	private DDrawnShape top;
	private DDrawnShape right;
	private DDrawnShape bottom;
	
	public DSideBorder(
			int leftWidth, int leftColor,
			int topWidth, int topColor,
			int rightWidth, int rightColor,
			int bottomWidth, int bottomColor) {
		this.leftWidth = leftWidth;
		this.leftColor = leftColor;
		this.topWidth = topWidth;
		this.topColor = topColor;
		this.rightWidth = rightWidth;
		this.rightColor = rightColor;
		this.bottomWidth = bottomWidth;
		this.bottomColor = bottomColor;
	}

	@Override
	public void update(DComponentContext context, DIRectangle bounds) {
		if (leftWidth > 0) {
			int x = bounds.x;
			if (left != null)
				left.close();
			left = context.strokePath(1, DPath.line(x, bounds.y, x, bounds.y + bounds.height), DTransform2D.IDENTITY, topColor, topWidth);
		}
		if (topWidth > 0) {
			int y = bounds.y;
			if (top != null)
				top.close();
			top = context.strokePath(1, DPath.line(bounds.x, y, bounds.x + bounds.width, y), DTransform2D.IDENTITY, topColor, topWidth);
		}
		if (rightWidth > 0) {
			int x = bounds.x + bounds.width - rightWidth;
			if (right != null)
				right.close();
			right = context.strokePath(1, DPath.line(x, bounds.y, x, bounds.y + bounds.height), DTransform2D.IDENTITY, topColor, topWidth);
		}
		if (bottomWidth > 0) {
			int y = bounds.y + bounds.height - bottomWidth;
			if (bottom != null)
				bottom.close();
			bottom = context.strokePath(1, DPath.line(bounds.x, y, bounds.x + bounds.width, y), DTransform2D.IDENTITY, topColor, topWidth);
		}
	}

	@Override
	public int getPaddingLeft() {
		return leftWidth;
	}

	@Override
	public int getPaddingRight() {
		return rightWidth;
	}

	@Override
	public int getPaddingTop() {
		return topWidth;
	}

	@Override
	public int getPaddingBottom() {
		return bottomWidth;
	}

	@Override
	public void close() {
		if (left != null)
			left.close();
		if (top != null)
			top.close();
		if (right != null)
			right.close();
		if (bottom != null)
			bottom.close();
	}
}
