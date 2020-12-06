/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;

/**
 * @author Hoofdgebruiker
 */
public class DRoundedRectangleShape implements DShape {
	private final float radiusTopLeft;
	private final float radiusTopRight;
	private final float radiusBottomLeft;
	private final float radiusBottomRight;

	public DRoundedRectangleShape(float radius) {
		this(radius, radius, radius, radius);
	}

	public DRoundedRectangleShape(float radiusTopLeft, float radiusTopRight, float radiusBottomLeft, float radiusBottomRight) {
		this.radiusTopLeft = radiusTopLeft;
		this.radiusTopRight = radiusTopRight;
		this.radiusBottomLeft = radiusBottomLeft;
		this.radiusBottomRight = radiusBottomRight;
	}

	@Override
	public DPath instance(DIRectangle bounds) {
		return DPath.roundedRectangle(bounds.x, bounds.y, bounds.width, bounds.height, radiusTopLeft, radiusTopRight, radiusBottomLeft, radiusBottomRight);
	}
}
