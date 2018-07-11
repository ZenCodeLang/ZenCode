/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class StatusBarStyle {
	public final int paddingTop;
	public final int paddingBottom;
	public final int paddingLeft;
	public final int paddingRight;
	public final DFont font;
	public final int backgroundColor;
	public final int textColor;
	
	public StatusBarStyle(DStyleDefinition style) {
		paddingTop = style.getDimension("paddingTop", new DDpDimension(4));
		paddingBottom = style.getDimension("paddingBottom", new DDpDimension(4));
		paddingLeft = style.getDimension("paddingLeft", new DDpDimension(4));
		paddingRight = style.getDimension("paddingRight", new DDpDimension(4));
		
		font = style.getFont("font", context -> new DFont(DFontFamily.UI, false, false, false, (int)(14 * context.getScale())));
		backgroundColor = style.getColor("backgroundColor", 0xFFF0F0F0);
		textColor = style.getColor("textColor", 0xFF000000);
	}
}
