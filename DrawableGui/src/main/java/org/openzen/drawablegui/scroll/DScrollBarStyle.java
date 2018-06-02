/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.scroll;

import org.openzen.drawablegui.style.DDimension;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DScrollBarStyle {
	public final int scrollBarBackgroundColor;
	public final int scrollBarNormalColor;
	public final int scrollBarHoverColor;
	public final int scrollBarPressColor;
	public final int width;
	
	public DScrollBarStyle(DStyleDefinition style) {
		this.scrollBarBackgroundColor = style.getColor("scrollbar.background.color", 0xFFF0F0F0);
		this.scrollBarNormalColor = style.getColor("scrollbar.color.normal", 0xFFCDCDCD);
		this.scrollBarHoverColor = style.getColor("scrollbar.color.hover", 0xFF888888);
		this.scrollBarPressColor = style.getColor("scrollbar.color.press", 0xFF666666);
		this.width = style.getDimension("width", new DDpDimension(12));
	}
}
