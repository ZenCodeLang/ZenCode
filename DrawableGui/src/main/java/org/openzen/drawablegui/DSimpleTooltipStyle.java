/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.border.DBorder;
import org.openzen.drawablegui.border.DCompositeBorder;
import org.openzen.drawablegui.border.DLineBorder;
import org.openzen.drawablegui.border.DPaddedBorder;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DSimpleTooltipStyle {
	public final DBorder border;
	public final DFont font;
	public final int backgroundColor;
	public final int textColor;
	
	public DSimpleTooltipStyle(DStyleDefinition style) {
		border = style.getBorder("border", context -> new DCompositeBorder(
				new DLineBorder(0xFF000000, 1),
				new DPaddedBorder(context.dp(4), context.dp(4), context.dp(4), context.dp(4))));
		font = style.getFont("font", context -> new DFont(DFontFamily.UI, false, false, false, context.sp(12)));
		backgroundColor = style.getColor("backgroundColor", 0xFFFFFFE1);
		textColor = style.getColor("textColor", 0xFF000000);
	}
}
