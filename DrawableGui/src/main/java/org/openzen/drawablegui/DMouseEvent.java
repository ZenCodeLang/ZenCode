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
public class DMouseEvent {
	public static final int BUTTON1 = 1;
	public static final int BUTTON2 = 2;
	public static final int BUTTON3 = 4;
	public static final int ALT = 256;
	public static final int CTRL = 512;
	public static final int SHIFT = 1024;
	public static final int META = 2048;
	public static final int ALT_GRAPH = 4096;
	
	public final int x;
	public final int y;
	public final int modifiers;
	public final int deltaZ;
	public final int clickCount;
	
	public DMouseEvent(int x, int y, int modifiers, int deltaZ, int clickCount) {
		this.x = x;
		this.y = y;
		this.modifiers = modifiers;
		this.deltaZ = deltaZ;
		this.clickCount = clickCount;
	}
	
	public boolean isSingleClick() {
		return clickCount == 1;
	}
	
	public boolean isDoubleClick() {
		return clickCount == 2;
	}
	
	public boolean isTripleClick() {
		return clickCount == 3;
	}
	
	public boolean has(int modifiers) {
		return (this.modifiers & modifiers) == modifiers;
	}
}
