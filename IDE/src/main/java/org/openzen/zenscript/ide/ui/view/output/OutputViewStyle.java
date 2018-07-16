/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.output;

import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.border.DBorder;
import org.openzen.drawablegui.border.DPaddedBorder;
import org.openzen.drawablegui.style.DRoundedRectangleShape;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DShape;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class OutputViewStyle {
	public final int backgroundColor;
	public final DBorder border;
	public final DShadow shadow;
	public final DShape shape;
	public final DFont font;
	
	public OutputViewStyle(DStyleDefinition style) {
		backgroundColor = style.getColor("backgroundColor", 0xFFFFFFFF);
		border = style.getBorder("border", context -> new DPaddedBorder(context.dp(8)));
		shadow = style.getShadow("shadow", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 3 * context.getScale()));
		shape = style.getShape("shape", context -> new DRoundedRectangleShape(context.dp(2)));
		font = style.getFont("font", context -> new DFont(DFontFamily.CODE, false, false, false, context.sp(14)));
	}
}
