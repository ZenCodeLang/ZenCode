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
public enum DAnchor {
	TOP_LEFT(0, 0),
	TOP_CENTER(0.5f, 0),
	TOP_RIGHT(1, 0),
	MIDDLE_LEFT(0, 0.5f),
	MIDDLE_CENTER(0.5f, 0.5f),
	MIDDLE_RIGHT(1, 0.5f),
	BOTTOM_LEFT(0, 1),
	BOTTOM_CENTER(0.5f, 1),
	BOTTOM_RIGHT(1, 1);
	
	public final float alignX;
	public final float alignY;
	
	DAnchor(float alignX, float alignY) {
		this.alignX = alignX;
		this.alignY = alignY;
	}
}
