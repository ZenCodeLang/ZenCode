/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class IconButtonControlStyle {
	public final int margin;
	public final int padding;
	public final int roundingRadius;
	
	public final int colorNormal;
	public final int colorHover;
	public final int colorPress;
	public final int colorDisabled;
	
	public final DShadow shadowNormal;
	public final DShadow shadowHover;
	public final DShadow shadowPress;
	public final DShadow shadowDisabled;
	
	public IconButtonControlStyle(DStyleDefinition style) {
		margin = style.getDimension("margin", new DDpDimension(3));
		padding = style.getDimension("padding", new DDpDimension(2));
		roundingRadius = style.getDimension("roundingRadius", new DDpDimension(4));
		
		colorNormal = style.getColor("colorNormal", 0);
		colorHover = style.getColor("colorHover", 0xFFFFFFFF);
		colorPress = style.getColor("colorPress", 0xFFF0F0F0);
		colorDisabled = style.getColor("colorDisabled", 0);
		
		shadowNormal = style.getShadow("shadowNormal", context -> DShadow.NONE);
		shadowHover = style.getShadow("shadowNormal", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 2.0f * context.getScale()));
		shadowPress = style.getShadow("shadowNormal", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 1.5f * context.getScale()));
		shadowDisabled = style.getShadow("shadowDisabled", context -> DShadow.NONE);
	}
}
