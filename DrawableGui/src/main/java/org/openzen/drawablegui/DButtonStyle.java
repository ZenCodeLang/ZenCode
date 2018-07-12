/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DButtonStyle {
	public final int paddingTop;
	public final int paddingLeft;
	public final int paddingRight;
	public final int paddingBottom;
	public final DFont font;
	public final int textColor;
	public final int textColorDisabled;
	
	public final int backgroundColorNormal;
	public final int backgroundColorHover;
	public final int backgroundColorPress;
	public final int backgroundColorDisabled;
	
	public final DShadow shadowNormal;
	public final DShadow shadowHover;
	public final DShadow shadowPress;
	public final DShadow shadowDisabled;
	
	public DButtonStyle(DStyleDefinition style) {
		this.paddingTop = style.getDimension("paddingTop", new DDpDimension(4));
		this.paddingBottom = style.getDimension("paddingBottom", new DDpDimension(4));
		this.paddingLeft = style.getDimension("paddingLeft", new DDpDimension(8));
		this.paddingRight = style.getDimension("paddingRight", new DDpDimension(8));
		font = style.getFont("font", context -> new DFont(DFontFamily.UI, false, false, false, (int)(14 * context.getTextScale())));
		textColor = style.getColor("textColor", 0xFF000000);
		textColorDisabled = style.getColor("textColorDisabled", 0xFF888888);
		
		backgroundColorNormal = style.getColor("backgroundColorNormal", 0xFFE1E1E1);
		backgroundColorHover = style.getColor("backgroundColorHover", 0xFFE5EFF8);
		backgroundColorPress = style.getColor("backgroundColorPress", 0xFFE5EFF8);
		backgroundColorDisabled = style.getColor("backgroundColorDisabled", 0xFFE5EFF8);
		
		shadowNormal = style.getShadow("shadowNormal", context -> new DShadow(0xFF888888, 0, 1f * context.getScale(), 4 * context.getScale()));
		shadowHover = style.getShadow("shadowHover", context -> new DShadow(0xFF888888, 0, 1f * context.getScale(), 4 * context.getScale()));
		shadowPress = style.getShadow("shadowPress", context -> new DShadow(0xFF888888, 0, 1f * context.getScale(), 3 * context.getScale()));
		shadowDisabled = style.getShadow("shadowDisabled", context -> new DShadow(0xFF888888, 0, 1f * context.getScale(), 3 * context.getScale()));
	}
}
