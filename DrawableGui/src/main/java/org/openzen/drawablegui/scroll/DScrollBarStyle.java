/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.scroll;

/**
 *
 * @author Hoofdgebruiker
 */
public class DScrollBarStyle {
	public static final DScrollBarStyle DEFAULT = new DScrollBarStyle(0xFFF0F0F0, 0xFFCDCDCD, 0xFF888888, 0xFF666666, 20);
	
	public final int scrollBarBackgroundColor;
	public final int scrollBarNormalColor;
	public final int scrollBarHoverColor;
	public final int scrollBarPressColor;
	public final int width;
	
	public DScrollBarStyle(
			int scrollBarBackgroundColor,
			int scrollBarNormalColor,
			int scrollBarHoverColor,
			int scrollBarPressColor,
			int width) {
		this.scrollBarBackgroundColor = scrollBarBackgroundColor;
		this.scrollBarNormalColor = scrollBarNormalColor;
		this.scrollBarHoverColor = scrollBarHoverColor;
		this.scrollBarPressColor = scrollBarPressColor;
		this.width = width;
	}
}
