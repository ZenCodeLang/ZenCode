/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.border.DBorder;
import org.openzen.drawablegui.border.DCompositeBorder;
import org.openzen.drawablegui.border.DLineBorder;
import org.openzen.drawablegui.border.DPaddedBorder;
import org.openzen.drawablegui.style.DBaseStyle;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 * @author Hoofdgebruiker
 */
public class TabbedViewStyle extends DBaseStyle {
	public final DFont tabFont;
	public final int tabFontColor;
	public final DBorder tabBorder;

	public final int tabBarSpacingLeft;
	public final int tabBarSpacingRight;
	public final int tabSpacing;

	public TabbedViewStyle(DStyleDefinition style) {
		super(style);

		tabFont = style.getFont("tabFont", context -> new DFont(DFontFamily.UI, false, false, false, (int) (12 * context.getScale())));
		tabFontColor = style.getColor("tabFontColor", 0xFF000000);
		tabBorder = style.getBorder("tabBorder", context -> new DCompositeBorder(new DLineBorder(0xFF888888, 1), new DPaddedBorder(context.dp(4))));

		tabBarSpacingLeft = style.getDimension("tabBarSpacingLeft", new DDpDimension(4));
		tabBarSpacingRight = style.getDimension("tabBarSpacingRight", new DDpDimension(4));
		tabSpacing = style.getDimension("tabSpacing", new DDpDimension(4));
	}
}
