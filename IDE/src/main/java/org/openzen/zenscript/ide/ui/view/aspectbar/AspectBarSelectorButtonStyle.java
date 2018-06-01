/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.aspectbar;

import org.openzen.drawablegui.dimension.DDimension;
import org.openzen.drawablegui.dimension.DDpDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class AspectBarSelectorButtonStyle {
	public final int width;
	public final int height;
	public final int roundingRadius;
	
	public final int colorNormal;
	public final int colorHover;
	public final int colorPress;
	public final int colorActive;
	
	public final int shadowColor;
	public final float shadowOffsetX;
	public final float shadowOffsetY;
	public final float shadowRadius;
	
	public AspectBarSelectorButtonStyle(DStyleDefinition style) {
		width = style.getDimension("width", new DDpDimension(24));
		height = style.getDimension("height", new DDpDimension(24));
		roundingRadius = style.getDimension("roundingRadius", new DDpDimension(4));
		
		colorNormal = style.getColor("colorNormal", 0xFFFFFFFF);
		colorHover = style.getColor("colorHover", 0xFFCCCCCC);
		colorPress = style.getColor("colorPress", 0xFFBBBBBB);
		colorActive = style.getColor("colorActive", 0xFFBBBBBB);
		
		shadowRadius = style.getDimension("shadowRadius", new DDpDimension(3));
		shadowColor = style.getColor("ashadowColor", 0xFF888888);
		shadowOffsetX = style.getDimension("shadowOffsetX", new DDpDimension(0));
		shadowOffsetY = style.getDimension("shadowOffsetY", new DDpDimension(0.5f));
	}
}
