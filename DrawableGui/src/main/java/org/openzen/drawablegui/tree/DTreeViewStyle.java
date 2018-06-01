/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.tree;

import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.dimension.DDimension;
import org.openzen.drawablegui.dimension.DDpDimension;
import org.openzen.drawablegui.DUIContext;

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
	public final DDimension padding;
	public final DDimension rowSpacing;
	public final DDimension fontSize;
	public final DDimension indent;
	public final DDimension iconTextSpacing;
	public final int nodeTextColor;
	public final int selectedNodeTextColor;
	public final int selectedBackgroundColor;
	public final DDimension selectedPaddingTop;
	public final DDimension selectedPaddingBottom;
	
	private DTreeViewStyle() {
		nodeOpenedIcon = ExpandedArrow.INSTANCE;
		nodeClosedIcon = CollapsedArrow.INSTANCE;
		backgroundColor = 0xFFFFFFFF;
		borderColor = 0xFF888888;
		padding = new DDpDimension(3);
		rowSpacing = new DDpDimension(2);
		fontSize = new DDpDimension(12.5f);  
		indent = new DDpDimension(12);
		iconTextSpacing = new DDpDimension(2);
		nodeTextColor = 0xFF000000;
		selectedNodeTextColor = 0xFFFFFFFF;
		selectedBackgroundColor = 0xFF007ACC;
		selectedPaddingTop = new DDpDimension(1);
		selectedPaddingBottom = new DDpDimension(1);
	}
	
	public Calculated forContext(DUIContext context) {
		return new Calculated(this, context);
	}
	
	public static class Calculated {
		public final DDrawable nodeOpenedIcon;
		public final DDrawable nodeClosedIcon;
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
		
		private Calculated(DTreeViewStyle style, DUIContext context) {
			nodeOpenedIcon = style.nodeOpenedIcon;
			nodeClosedIcon = style.nodeClosedIcon;
			backgroundColor = style.backgroundColor;
			borderColor = style.borderColor;
			padding = style.padding.evalInt(context);
			rowSpacing = style.rowSpacing.evalInt(context);
			font = new DFont(DFontFamily.UI, false, false, false, style.fontSize.evalInt(context));
			indent = style.indent.evalInt(context);
			iconTextSpacing = style.iconTextSpacing.evalInt(context);
			nodeTextColor = style.nodeTextColor;
			selectedNodeTextColor = style.selectedNodeTextColor;
			selectedBackgroundColor = style.selectedBackgroundColor;
			selectedPaddingTop = style.selectedPaddingTop.evalInt(context);
			selectedPaddingBottom = style.selectedPaddingBottom.evalInt(context);
		}
	}
}
