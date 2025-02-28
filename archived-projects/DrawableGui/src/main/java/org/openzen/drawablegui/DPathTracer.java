/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

/**
 * @author Hoofdgebruiker
 */
public interface DPathTracer {
	public void moveTo(float x, float y);

	public void lineTo(float x, float y);

	public void bezierCubic(float x1, float y1, float x2, float y2, float x3, float y3);

	public void bezierQuadratic(float x1, float y1, float x2, float y2);

	public void close();
}
