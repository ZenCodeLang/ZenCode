/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.border.DCompositeBorder;
import org.openzen.drawablegui.border.DLineBorder;
import org.openzen.drawablegui.border.DPaddedBorder;
import org.openzen.drawablegui.style.DBaseStyle;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 * @author Hoofdgebruiker
 */
public class DInputFieldStyle extends DBaseStyle {
	public final int color;
	public final DFont font;

	public final int cursorColor;
	public final int cursorWidth;

	public final int selectionColor;

	public DInputFieldStyle(DStyleDefinition style) {
		super(style, context -> new DCompositeBorder(new DLineBorder(0xFFABADB3, 1), new DPaddedBorder(context.dp(2))), 0xFFFFFFFF);

		color = style.getColor("color", 0xFF000000);
		font = style.getFont("font", context -> new DFont(DFontFamily.UI, false, false, false, (int) (14 * context.getScale())));

		cursorColor = style.getColor("cursorColor", 0xFF000000);
		cursorWidth = style.getDimension("cursorWidth", new DDpDimension(1));

		selectionColor = style.getColor("selectionColor", 0xFFB0C5E3);
	}
}
