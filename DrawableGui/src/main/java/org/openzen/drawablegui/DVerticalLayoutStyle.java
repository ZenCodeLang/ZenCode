/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DVerticalLayoutStyle {
	public final int spacing;
	
	public final int paddingTop;
	public final int paddingLeft;
	public final int paddingRight;
	public final int paddingBottom;
	
	public DVerticalLayoutStyle(DStyleDefinition style) {
		spacing = style.getDimension("spacing", new DDpDimension(8));
		
		paddingTop = style.getDimension("paddingTop", new DDpDimension(8));
		paddingLeft = style.getDimension("paddingLeft", new DDpDimension(8));
		paddingRight = style.getDimension("paddingRight", new DDpDimension(8));
		paddingBottom = style.getDimension("paddingBottom", new DDpDimension(8));
	}
}
