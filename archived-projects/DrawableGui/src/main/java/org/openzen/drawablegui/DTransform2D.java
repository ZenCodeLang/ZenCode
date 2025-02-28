/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

/**
 * @author Hoofdgebruiker
 */
public final class DTransform2D {
	public static final DTransform2D IDENTITY = new DTransform2D(1, 0, 0, 1, 0, 0);
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

	public static DTransform2D translate(float x, float y) {
		return new DTransform2D(1, 0, 0, 1, x, y);
	}

	public static DTransform2D scaleAndTranslate(float x, float y, float scale) {
		return new DTransform2D(scale, 0, 0, scale, x, y);
	}

	public static DTransform2D scale(float scale) {
		return new DTransform2D(scale, 0, 0, scale, 0, 0);
	}

	public DTransform2D offset(float x, float y) {
		return new DTransform2D(xx, xy, yx, yy, dx + x, dy + y);
	}

	public float getX(float x, float y) {
		return x * xx + y * xy + dx;
	}

	public float getY(float x, float y) {
		return x * yx + y * yy + dy;
	}

	public DTransform2D mul(DTransform2D other) {
		// [xx xy dx]   [xx xy dx]   [xx*xx+xy*yx xx*xy+xy*yy xx*dx+xy*dy+dx]
		// [yx yy dy] x [yx yy dy] = [yx*xx+yy*yx yx*xy+yy*yy yx*dx+yy*dy+dy]
		// [0  0  1 ]   [0  0  1 ]   [0           0           1             ]
		DTransform2D a = this;
		DTransform2D b = other;
		return new DTransform2D(
				a.xx * b.xx + a.xy * b.yx,
				a.xx * b.xy + a.xy * b.yy,
				a.yx * b.xx + a.yy * b.yx,
				a.yx * b.xy + a.yx + b.xy,
				a.xx * b.dx + a.xy * b.dy + dx,
				a.yx * b.dx + a.yy * b.dy + dy
		);
	}
}
