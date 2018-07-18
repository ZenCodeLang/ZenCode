/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.output;

import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.border.DPaddedBorder;
import org.openzen.drawablegui.style.DBaseStyle;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class OutputViewStyle extends DBaseStyle {
	public final DFont font;
	
	public OutputViewStyle(DStyleDefinition style) {
		super(style, context -> new DPaddedBorder(context.dp(8)), 0);
		
		font = style.getFont("font", context -> new DFont(DFontFamily.CODE, false, false, false, context.sp(14)));
	}
}
