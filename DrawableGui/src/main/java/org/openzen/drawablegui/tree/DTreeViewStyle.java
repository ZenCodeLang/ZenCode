/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.tree;

import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;

/**
 *
 * @author Hoofdgebruiker
 */
public class DTreeViewStyle {
	public static DTreeViewStyle DEFAULT = new DTreeViewStyle();
	
	public final DDrawable nodeOpenedIcon;
	public final DDrawable nodeClosedIcon;
	public final int backgroundColor;
	public final int borderColor;
	public final int padding;
	public final int rowSpacing;
	public final DFont font;
	public final int indent;
	public final int textShift;
	public final int iconTextSpacing;
	public final int nodeTextColor;
	public final int selectedNodeTextColor;
	public final int selectedBackgroundColor;
	public final int selectedPaddingTop;
	public final int selectedPaddingBottom;
	
	private DTreeViewStyle() {
		nodeOpenedIcon = ExpandedArrow.INSTANCE;
		nodeClosedIcon = CollapsedArrow.INSTANCE;
		backgroundColor = 0xFFFFFFFF;
		borderColor = 0xFF888888;
		padding = 5;
		rowSpacing = 10;
		font = new DFont(DFontFamily.UI, false, false, false, 22);
		indent = 20;
		textShift = -4;
		iconTextSpacing = 3;
		nodeTextColor = 0xFF000000;
		selectedNodeTextColor = 0xFFFFFFFF;
		selectedBackgroundColor = 0xFF007ACC;
		selectedPaddingTop = 3;
		selectedPaddingBottom = 3;
	}
}
