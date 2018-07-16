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
public interface DPath {
	public static DPath line(float x1, float y1, float x2, float y2) {
		return tracer -> {
			tracer.moveTo(x1, y1);
			tracer.lineTo(x2, y2);
		};
	}
	
	public static DPath circle(float x, float y, float radius) {
		// see http://spencermortensen.com/articles/bezier-circle/
		return tracer -> {
			float c = radius * 0.551915024494f;
			tracer.moveTo(x, y + radius);
			tracer.bezierCubic(
					x + c, y + radius,
					x + radius, y + c,
					x + radius, y);
			tracer.bezierCubic(
					x + radius, y - c,
					x + c, y - radius,
					x, y - radius);
			tracer.bezierCubic(
					x - c, y - radius,
					x - radius, y - c,
					x - radius, y);
			tracer.bezierCubic(
					x - radius, y + c,
					x - c, y + radius,
					x, y + radius);
		};
	}
	
	public static DPath rectangle(float x, float y, float width, float height) {
		return tracer -> {
			tracer.moveTo(x, y);
			tracer.lineTo(x + width, y);
			tracer.lineTo(x + width, y + height);
			tracer.lineTo(x, y + height);
			tracer.close();
		};
	}
	
	public static DPath roundedRectangle(float x, float y, float width, float height, float radius) {
		if (radius < 0.01f)
			return rectangle(x, y, width, height);
		
		return tracer -> {
			float c = radius - radius * 0.551915024494f;
			tracer.moveTo(x + width - radius, y + height);
			tracer.bezierCubic(
					x + width - c, y + height,
					x + width, y + height - c,
					x + width, y + height - radius);
			tracer.lineTo(x + width, y + radius);
			tracer.bezierCubic(
					x + width, y + c,
					x + width - c, y,
					x + width - radius, y);
			tracer.lineTo(x + radius, y);
			tracer.bezierCubic(
					x + c, y,
					x, y + c,
					x, y + radius);
			tracer.lineTo(x, y + height - radius);
			tracer.bezierCubic(
					x, y + height - c,
					x + c, y + height,
					x + radius, y + height);
			tracer.close();
		};
	}
	
	public static DPath roundedRectangle(float x, float y, float width, float height, float radiusTopLeft, float radiusTopRight, float radiusBottomLeft, float radiusBottomRight) {
		return tracer -> {
			float cTopLeft = radiusTopLeft - radiusTopLeft * 0.551915024494f;
			float cTopRight = radiusTopRight - radiusTopRight * 0.551915024494f;
			float cBottomLeft = radiusBottomLeft - radiusBottomLeft * 0.551915024494f;
			float cBottomRight = radiusBottomRight - radiusBottomRight * 0.551915024494f;
			
			tracer.moveTo(x + width - radiusBottomRight, y + height);
			if (radiusBottomRight > 0.01f) {
				tracer.bezierCubic(
						x + width - cBottomRight, y + height,
						x + width, y + height - cBottomRight,
						x + width, y + height - radiusBottomRight);
			}
			tracer.lineTo(x + width, y + radiusTopRight);
			if (radiusTopRight > 0.01f) {
				tracer.bezierCubic(
						x + width, y + cTopRight,
						x + width - cTopRight, y,
						x + width - radiusTopRight, y);
			}
			tracer.lineTo(x + radiusTopLeft, y);
			if (radiusTopLeft > 0.01f) {
				tracer.bezierCubic(
						x + cTopLeft, y,
						x, y + cTopLeft,
						x, y + radiusTopLeft);
			}
			tracer.lineTo(x, y + height - radiusBottomLeft);
			if (radiusBottomLeft > 0.01f) {
				tracer.bezierCubic(
						x, y + height - cBottomLeft,
						x + cBottomLeft, y + height,
						x + radiusBottomLeft, y + height);
			}
			tracer.close();
		};
	}
	
	void trace(DPathTracer tracer);
}
