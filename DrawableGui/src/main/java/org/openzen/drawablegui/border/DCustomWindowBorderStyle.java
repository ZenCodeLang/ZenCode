/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.border;

import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DPxDimension;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 * @author Hoofdgebruiker
 */
public class DCustomWindowBorderStyle {
	public final int padding;
	public final DShadow shadow;
	public final int borderWidth;
	public final int backgroundColor;

	public final int focusedBorderColor;
	public final int inactiveBorderColor;

	public DCustomWindowBorderStyle(DStyleDefinition style) {
		padding = style.getDimension("padding", new DDpDimension(8));
		shadow = style.getShadow("shadow", context -> new DShadow(0x80888888, 0, 0, 6 * context.getScale()));
		borderWidth = style.getDimension("borderWidth", new DPxDimension(1));
		backgroundColor = style.getColor("backgroundColor", 0xFFEEEEEE);

		focusedBorderColor = style.getColor("focusedBorderColor", 0xFF18B0FB);
		inactiveBorderColor = style.getColor("inactiveBorderColor", 0xFF515252);
	}
}
