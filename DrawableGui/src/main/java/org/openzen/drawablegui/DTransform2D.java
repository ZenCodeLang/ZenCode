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
public final class DTransform2D {
	public static final DTransform2D IDENTITY = new DTransform2D(1, 0, 0, 1, 0, 0);
	
	public static DTransform2D translate(float x, float y) {
		return new DTransform2D(1, 0, 0, 1, x, y);
	}
	
	public static DTransform2D scale(float scale) {
		return new DTransform2D(scale, 0, 0, scale, 0, 0);
	}
	
	public final float xx;
	public final float xy;
	public final float yx;
	public final float yy;
	public final float dx;
	public final float dy;
	
	public DTransform2D(float xx, float xy, float yx, float yy, float dx, float dy) {
		this.xx = xx;
		this.xy = xy;
		this.yx = yx;
		this.yy = yy;
		this.dx = dx;
		this.dy = dy;
	}
}
