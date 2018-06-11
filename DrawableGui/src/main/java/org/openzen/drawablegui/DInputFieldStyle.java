/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DInputFieldStyle {
	public final int backgroundColor;
	public final int paddingTop;
	public final int paddingLeft;
	public final int paddingRight;
	public final int paddingBottom;
	
	public final int color;
	public final DFont font;
	
	public final int cursorColor;
	public final int cursorWidth;
	
	public final int selectionColor;
	
	public DInputFieldStyle(DStyleDefinition style) {
		backgroundColor = style.getColor("backgroundColor", 0);
		paddingTop = style.getDimension("paddingTop", new DDpDimension(2));
		paddingBottom = style.getDimension("paddingBottom", new DDpDimension(2));
		paddingLeft = style.getDimension("paddingLeft", new DDpDimension(2));
		paddingRight = style.getDimension("paddingRight", new DDpDimension(2));
		
		color = style.getColor("color", 0xFF000000);
		font = style.getFont("font", context -> new DFont(DFontFamily.UI, false, false, false, (int)(14 * context.getScale())));
		
		cursorColor = style.getColor("cursorColor", 0xFF000000);
		cursorWidth = style.getDimension("cursorWidth", new DDpDimension(1));
		
		selectionColor = style.getColor("selectionColor", 0xFFB0C5E3);
	}
}
