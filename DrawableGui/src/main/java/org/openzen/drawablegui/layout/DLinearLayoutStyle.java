/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.layout;

import org.openzen.drawablegui.style.DBaseStyle;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DLinearLayoutStyle extends DBaseStyle {
	public final int spacing;
	
	public DLinearLayoutStyle(DStyleDefinition style) {
		super(style);
		
		spacing = style.getDimension("spacing", new DDpDimension(8));
	}
}
