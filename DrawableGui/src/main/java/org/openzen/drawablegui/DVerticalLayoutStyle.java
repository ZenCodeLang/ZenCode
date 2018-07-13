/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.border.DBorder;
import org.openzen.drawablegui.border.DPaddedBorder;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DVerticalLayoutStyle {
	public final int spacing;
	public final DBorder border;
	public final int backgroundColor;
	
	public DVerticalLayoutStyle(DStyleDefinition style) {
		spacing = style.getDimension("spacing", new DDpDimension(8));
		border = style.getBorder("border", context -> new DPaddedBorder(context.dp(8)));
		backgroundColor = style.getColor("backgroundColor", 0);
	}
}
