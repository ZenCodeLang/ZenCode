/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.tree;

import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.style.DDimension;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 * @author Hoofdgebruiker
 */
public class DTreeViewStyle {
	public final int backgroundColor;
	public final int borderColor;
	public final int padding;
	public final int rowSpacing;
	public final DFont font;
	public final int indent;
	public final int iconTextSpacing;
	public final int nodeTextColor;
	public final int selectedNodeTextColor;
	public final int selectedBackgroundColor;
	public final int selectedPaddingTop;
	public final int selectedPaddingBottom;
	public final int selectedPaddingLeft;
	public final int selectedPaddingRight;

	public DTreeViewStyle(DStyleDefinition style) {
		backgroundColor = style.getColor("backgroundColor", 0);
		borderColor = style.getColor("borderColor", 0xFF888888);
		padding = style.getDimension("padding", new DDpDimension(3));
		rowSpacing = style.getDimension("rowSpacing", new DDpDimension(2));
		font = style.getFont("font", context -> new DFont(DFontFamily.UI, false, false, false, context.sp(13)));
		indent = style.getDimension("indent", new DDpDimension(12));
		iconTextSpacing = style.getDimension("iconTextSpacing", new DDpDimension(2));
		nodeTextColor = style.getColor("nodeTextColor", 0xFF000000);
		selectedNodeTextColor = style.getColor("selectedNodeTextColor", 0xFFFFFFFF);
		selectedBackgroundColor = style.getColor("selectedBackgroundColor", 0xFF007ACC);
		selectedPaddingTop = style.getDimension("selectedPaddingTop", new DDpDimension(1));
		selectedPaddingBottom = style.getDimension("selectedPaddingBottom", new DDpDimension(1));
		selectedPaddingLeft = style.getDimension("selectedPaddingLeft", new DDpDimension(2));
		selectedPaddingRight = style.getDimension("selectedPaddingRight", new DDpDimension(2));
	}
}
