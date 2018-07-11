/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.aspectbar;

import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DShadow;
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
	
	public final int aspectSelectorButtonSpacing;
	public final int toolbarTitleToControlsSpacing;
	
	public final DShadow aspectBarShadow;
	public final int aspectBarPaddingTop;
	
	public final int controlPaddingTop;
	public final int controlPaddingBottom;
	
	public final DFont activeToolbarTitleFont;
	public final int activeToolbarTitleColor;
	
	public final int windowControlSpacingLeft;
	public final int windowControlSpacingBottom;
	public final DShadow windowControlShadow;
	
	public AspectBarStyle(DStyleDefinition style) {
		height = style.getDimension("height", new DDpDimension(32));
		aspectSelectorPaddingLeft = style.getDimension("aspectSelectorPaddingLeft", new DDpDimension(4));
		aspectSelectorToToolbarSpacing = style.getDimension("aspectSelectorToToolbarSpacing", new DDpDimension(16));
		toolbarTitleToControlsSpacing = style.getDimension("toolbarTitleToControlsSpacing", new DDpDimension(8));
		aspectSelectorBottomSize = style.getDimension("aspectSelectorBottomSize", new DDpDimension(4));
		backgroundColor = style.getColor("backgroundColor", 0xFFCCCCCC); // 0xFFF0F0F0
		foregroundColor = style.getColor("foregroundColor", 0xFFFFFFFF);
		
		aspectSelectorButtonSpacing = style.getDimension("aspectSelectorButtonSpacing", new DDpDimension(4));
		
		aspectBarShadow = style.getShadow("aspectBarShadow", context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 2 * context.getScale()));
		aspectBarPaddingTop = style.getDimension("aspectBarPaddingTop", new DDpDimension(4));
		
		controlPaddingTop = style.getDimension("controlPaddingTop", new DDpDimension(2));
		controlPaddingBottom = style.getDimension("controlPaddingBottom", new DDpDimension(2));
		
		activeToolbarTitleFont = style.getFont("activeToolbarTitleFont", context -> new DFont(DFontFamily.UI, false, false, false, (int)(12.0f * context.getScale())));
		activeToolbarTitleColor = style.getColor("activeToolbarTitleColor", 0xFF888888);
		
		windowControlSpacingLeft = style.getDimension("windowControlSpacingLeft", new DDpDimension(4));
		windowControlSpacingBottom = style.getDimension("windowControlSpacingBottom", new DDpDimension(4));
		//windowControlShadow = style.getShadow("windowControlShadow", context -> DShadow.NONE);
		windowControlShadow = style.getShadow("windowControlShadow", context -> new DShadow(0x80888888, 0, 0, 2 * context.getScale()));
	}
}
