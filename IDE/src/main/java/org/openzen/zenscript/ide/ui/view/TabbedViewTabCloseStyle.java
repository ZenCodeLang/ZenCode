/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedViewTabCloseStyle {
	public final int size;
	
	public TabbedViewTabCloseStyle(DStyleDefinition style) {
		size = style.getDimension("size", new DDpDimension(16));
	}
}
