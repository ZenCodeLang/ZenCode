/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.editor;

/**
 *
 * @author Hoofdgebruiker
 */
public class SourceEditorStyle {
	public static final SourceEditorStyle DEFAULT = new SourceEditorStyle(3, 0xFFE9EFF8, 0xFFB0C5E3, 2, 2);
	
	public final int extraLineSpacing;
	public final int currentLineHighlight;
	public final int selectionColor;
	public final int selectionPaddingTop;
	public final int selectionPaddingBottom;
	
	public SourceEditorStyle(
			int extraLineSpacing,
			int currentLineHighlight,
			int selectionColor,
			int selectionPaddingTop,
			int selectionPaddingBottom) {
		this.extraLineSpacing = extraLineSpacing;
		this.currentLineHighlight = currentLineHighlight;
		this.selectionColor = selectionColor;
		this.selectionPaddingTop = selectionPaddingTop;
		this.selectionPaddingBottom = selectionPaddingBottom;
	}
}
