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
	
	public static DPath roundedRectangle(float x, float y, float width, float height, float radius) {
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
	
	void trace(DPathTracer tracer);
}
