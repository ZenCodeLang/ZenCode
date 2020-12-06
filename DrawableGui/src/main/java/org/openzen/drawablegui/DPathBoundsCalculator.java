/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

/**
 * @author Hoofdgebruiker
 */
public class DPathBoundsCalculator implements DPathTracer {
	private final DTransform2D transform;
	private float minX = Float.MAX_VALUE;
	private float minY = Float.MAX_VALUE;
	private float maxX = Float.MIN_VALUE;
	private float maxY = Float.MIN_VALUE;

	private DPathBoundsCalculator(DTransform2D transform) {
		this.transform = transform;
	}

	public static DIRectangle getBounds(DPath path, DTransform2D transform) {
		DPathBoundsCalculator calculator = new DPathBoundsCalculator(transform);
		path.trace(calculator);
		return new DIRectangle(
				(int) calculator.minX,
				(int) calculator.minY,
				(int) Math.ceil(calculator.maxX - calculator.minX),
				(int) Math.ceil(calculator.maxY - calculator.minY));
	}

	private void add(float x, float y) {
		float tx = transform.getX(x, y);
		float ty = transform.getY(x, y);

		minX = Math.min(minX, tx);
		minY = Math.min(minY, ty);
		maxX = Math.max(maxX, tx);
		maxY = Math.max(maxY, ty);
	}

	@Override
	public void moveTo(float x, float y) {
		add(x, y);
	}

	@Override
	public void lineTo(float x, float y) {
		add(x, y);
	}

	@Override
	public void bezierCubic(float x1, float y1, float x2, float y2, float x3, float y3) {
		add(x1, y1);
		add(x2, y2);
		add(x3, y3);
	}

	@Override
	public void bezierQuadratic(float x1, float y1, float x2, float y2) {
		add(x1, y1);
		add(x2, y2);
	}

	@Override
	public void close() {

	}
}
