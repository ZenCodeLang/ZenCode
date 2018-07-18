/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.border.DCompositeBorder;
import org.openzen.drawablegui.border.DLineBorder;
import org.openzen.drawablegui.border.DPaddedBorder;
import org.openzen.drawablegui.style.DBaseStyle;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DPxDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedViewTabStyle extends DBaseStyle {
	public final DFont tabFont;
	public final int tabFontColor;
	
	public final int tabColorNormal;
	public final int tabColorHover;
	public final int tabColorPress;
	public final int tabColorActive;
	
	public final int closeIconSize;
	public final int closeIconPadding;
	
	public final int updatedDiameter;
	public final int updatedPadding;
	public final int updatedColor;
	
	public TabbedViewTabStyle(DStyleDefinition style) {
		super(style, context -> new DCompositeBorder(new DLineBorder(0xFF888888, 1), new DPaddedBorder(context.dp(4))), 0);
		tabFont = style.getFont("tabFont", context -> new DFont(DFontFamily.UI, false, false, false, (int)(12 * context.getScale())));
		tabFontColor = style.getColor("tabFontColor", 0xFF000000);
		
		tabColorNormal = style.getColor("tabColorNormal", 0xFFEEEEEE);
		tabColorHover = style.getColor("tabColorHover", 0xFFFFFFFF);
		tabColorPress = style.getColor("tabColorPress", 0xFFF0F0F0);
		tabColorActive = style.getColor("tabColorActive", 0xFFFFFFFF);
		
		closeIconSize = style.getDimension("closeIconSize", new DDpDimension(16));
		closeIconPadding = style.getDimension("closeIconPadding", new DDpDimension(6));
		
		updatedDiameter = style.getDimension("updatedDiameter", new DDpDimension(6));
		updatedPadding = style.getDimension("updatedPadding", new DDpDimension(8));
		updatedColor = style.getColor("updatedColor", 0xFF000000);
	}
}
