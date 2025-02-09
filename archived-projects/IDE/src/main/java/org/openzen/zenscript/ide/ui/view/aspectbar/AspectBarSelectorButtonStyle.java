/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.aspectbar;

import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
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

	public final DShadow shadowNormal;
	public final DShadow shadowHover;
	public final DShadow shadowPress;
	public final DShadow shadowActive;

	public AspectBarSelectorButtonStyle(DStyleDefinition style) {
		width = style.getDimension("width", new DDpDimension(24));
		height = style.getDimension("height", new DDpDimension(24));
		roundingRadius = style.getDimension("roundingRadius", new DDpDimension(4));

		colorNormal = style.getColor("colorNormal", 0xFFFFFFFF);
		colorHover = style.getColor("colorHover", 0xFFE0E0E0);
		colorPress = style.getColor("colorPress", 0xFFCCCCCC);
		colorActive = style.getColor("colorActive", 0xFFF0F0F0);

		shadowNormal = style.getShadow("shadowNormal", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 3 * context.getScale()));
		shadowHover = style.getShadow("shadowNormal", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 3 * context.getScale()));
		shadowPress = style.getShadow("shadowNormal", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 3 * context.getScale()));
		shadowActive = style.getShadow("shadowNormal", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 3 * context.getScale()));
	}
}
