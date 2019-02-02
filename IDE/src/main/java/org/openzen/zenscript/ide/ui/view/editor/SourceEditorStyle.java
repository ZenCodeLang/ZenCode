/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.editor;

import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DPxDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class SourceEditorStyle {
	public final int backgroundColor;
	public final int lineBarBackgroundColor;
	public final int lineBarStrokeColor;
	public final int lineBarStrokeWidth;
	public final int lineBarMinWidth;
	public final int lineBarSpacingLeft;
	public final int lineBarSpacingRight;
	public final int lineBarMargin;
	public final int lineBarTextColor;
	public final int extraLineSpacing;
	public final int currentLineHighlight;
	public final int selectionColor;
	public final int selectionPaddingTop;
	public final int selectionPaddingBottom;
	public final int cursorWidth;
	public final int cursorColor;
	
	public final int errorWavyLineColor;
	
	public SourceEditorStyle(DStyleDefinition style) {
		this.backgroundColor = style.getColor("backgroundColor", 0xFFFFFFFF);
		this.lineBarBackgroundColor = style.getColor("lineBarBackgroundColor", 0xFFE9E8E2);
		this.lineBarStrokeColor = style.getColor("lineBarStrokeColor", 0xFFA0A0A0);
		this.lineBarStrokeWidth = style.getDimension("lineBarStrokeWidth", new DPxDimension(1));
		this.lineBarMinWidth = style.getDimension("lineBarMinWidth", new DDpDimension(30));
		this.lineBarSpacingLeft = style.getDimension("lineBarSpacingLeft", new DDpDimension(5));
		this.lineBarSpacingRight = style.getDimension("lineBarSpacingRight", new DDpDimension(2));
		this.lineBarMargin = style.getDimension("lineBarMargin", new DDpDimension(7));
		this.lineBarTextColor = style.getColor("lineBarTextColor", 0xFFA0A0A0);
		
		this.extraLineSpacing = style.getDimension("extraLineSpacing", new DDpDimension(2));
		this.currentLineHighlight = style.getColor("currentLineHighlight", 0xFFE9EFF8);
		this.selectionColor = style.getColor("selectionColor", 0xFFB0C5E3);
		this.selectionPaddingTop = style.getDimension("selectionPaddingTop", new DDpDimension(1));
		this.selectionPaddingBottom = style.getDimension("selectionPaddingBottom", new DDpDimension(1));
		
		this.cursorWidth = style.getDimension("cursorWidth", new DDpDimension(1));
		this.cursorColor = style.getColor("cursorColor", 0xFF000000);
		
		this.errorWavyLineColor = style.getColor("errorWavyLineColor", 0xFFFF0000);
	}
}
