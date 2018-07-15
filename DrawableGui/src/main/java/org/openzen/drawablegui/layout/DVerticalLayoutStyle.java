/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.layout;

import org.openzen.drawablegui.border.DBorder;
import org.openzen.drawablegui.border.DPaddedBorder;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DMargin;
import org.openzen.drawablegui.style.DPxDimension;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DVerticalLayoutStyle {
	public final int spacing;
	public final DBorder border;
	public final int backgroundColor;
	public final DMargin margin;
	public final int cornerRadius;
	public final DShadow shadow;
	
	public DVerticalLayoutStyle(DStyleDefinition style) {
		spacing = style.getDimension("spacing", new DDpDimension(8));
		border = style.getBorder("border", context -> new DPaddedBorder(context.dp(8)));
		backgroundColor = style.getColor("backgroundColor", 0);
		margin = style.getMargin("margin", DMargin.EMPTY_ELEMENT);
		cornerRadius = style.getDimension("cornerRadius", DPxDimension.ZERO);
		shadow = style.getShadow("shadow", DShadow.NONE_ELEMENT);
	}
}
