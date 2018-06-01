/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.aspectbar;

import org.openzen.drawablegui.dimension.DDpDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class AspectBarStyle {
	public final int height;
	public final int aspectSelectorPaddingLeft;
	public final int aspectSelectorToToolbarSpacing;
	public final int aspectSelectorBottomSize;
	public final int backgroundColor;
	public final int foregroundColor;
	
	public final int aspectSelectorButtonRadius;
	public final int aspectSelectorButtonSpacing;
	public final int aspectSelectorButtonShadowRadius;
	public final int aspectSelectorButtonShadowColor;
	public final float aspectSelectorButtonShadowOffsetX;
	public final float aspectSelectorButtonShadowOffsetY;
	
	public final int aspectBarShadowRadius;
	public final int aspectBarShadowColor;
	public final float aspectBarShadowOffsetX;
	public final float aspectBarShadowOffsetY;
	public final int aspectBarPaddingTop;
	
	public final int controlPaddingTop;
	public final int controlPaddingBottom;
	
	public AspectBarStyle(DStyleDefinition style) {
		height = style.getDimension("height", new DDpDimension(32));
		aspectSelectorPaddingLeft = style.getDimension("aspectSelectorPaddingLeft", new DDpDimension(4));
		aspectSelectorToToolbarSpacing = style.getDimension("aspectSelectorToToolbarSpacing", new DDpDimension(16));
		aspectSelectorBottomSize = style.getDimension("aspectSelectorBottomSize", new DDpDimension(4));
		backgroundColor = style.getColor("backgroundColor", 0xFFF0F0F0);
		foregroundColor = style.getColor("foregroundColor", 0xFFFFFFFF);
		
		aspectSelectorButtonRadius = style.getDimension("aspectSelectorButtonRadius", new DDpDimension(12));
		aspectSelectorButtonSpacing = style.getDimension("aspectSelectorButtonSpacing", new DDpDimension(4));
		aspectSelectorButtonShadowRadius = style.getDimension("aspectSelectorButtonShadowRadius", new DDpDimension(3));
		aspectSelectorButtonShadowColor = style.getColor("aspectSelectorButtonShadowColor", 0xFF888888);
		aspectSelectorButtonShadowOffsetX = style.getDimension("aspectSelectorButtonShadowOffsetX", new DDpDimension(0));
		aspectSelectorButtonShadowOffsetY = style.getDimension("aspectSelectorButtonShadowOffsetY", new DDpDimension(0.5f));
		
		aspectBarShadowRadius = style.getDimension("aspectBarShadowRadius", new DDpDimension(2));
		aspectBarShadowColor = style.getColor("aspectBarShadowColor", 0xFF888888);
		aspectBarShadowOffsetX = style.getDimension("aspectBarShadowOffsetX", new DDpDimension(0));
		aspectBarShadowOffsetY = style.getDimension("aspectBarShadowOffsetX", new DDpDimension(0.5f));
		aspectBarPaddingTop = style.getDimension("aspectBarPaddingTop", new DDpDimension(2));
		
		controlPaddingTop = style.getDimension("controlPaddingTop", new DDpDimension(2));
		controlPaddingBottom = style.getDimension("controlPaddingBottom", new DDpDimension(2));
	}
}
