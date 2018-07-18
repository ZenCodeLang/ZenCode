/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

/**
 *
 * @author Hoofdgebruiker
 */
public class DShadow {
	public static final DShadow NONE = new DShadow(0, 0, 0, 0);
	public static final DShadowElement NONE_ELEMENT = context -> NONE;
	
	public final int color;
	public final float offsetX;
	public final float offsetY;
	public final float radius;
	
	public DShadow(int color, float offsetX, float offsetY, float radius) {
		this.color = color;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.radius = radius;
	}
}
