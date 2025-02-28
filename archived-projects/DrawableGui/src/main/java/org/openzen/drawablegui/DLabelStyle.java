/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.border.DBorder;
import org.openzen.drawablegui.border.DPaddedBorder;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 * @author Hoofdgebruiker
 */
public class DLabelStyle {
	public final DBorder border;
	public final DFont font;
	public final int color;

	public DLabelStyle(DStyleDefinition style) {
		border = style.getBorder("border", context -> new DPaddedBorder(
				(int) (4 * context.getScale()),
				(int) (4 * context.getScale()),
				(int) (4 * context.getScale()),
				(int) (4 * context.getScale())));
		font = style.getFont("font", context -> new DFont(DFontFamily.UI, false, false, false, (int) (14 * context.getTextScale())));
		color = style.getColor("color", 0xFF000000);
	}
}
