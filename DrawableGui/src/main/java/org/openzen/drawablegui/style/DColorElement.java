/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

/**
 * @author Hoofdgebruiker
 */
public class DColorElement implements DStyleElement {
	private final int color;

	public DColorElement(int color) {
		this.color = color;
	}

	@Override
	public int asColor() {
		return color;
	}
}
