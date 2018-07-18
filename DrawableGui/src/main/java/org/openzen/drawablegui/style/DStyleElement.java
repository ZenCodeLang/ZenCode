/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import java.util.function.Function;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.border.DBorder;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DStyleElement {
	default DDimension asDimension() {
		throw new RuntimeException("Not a dimension!");
	}
	
	default int asColor() {
		throw new RuntimeException("Not a color!");
	}
	
	default DFontElement asFont() {
		throw new RuntimeException("Not a font!");
	}
	
	default DBorderElement asBorder() {
		throw new RuntimeException("Not a border!");
	}
	
	default DMarginElement asMargin() {
		throw new RuntimeException("Not a margin!");
	}
	
	default DShadowElement asShadow() {
		throw new RuntimeException("Not a shadow!");
	}
	
	default DShapeElement asShape() {
		throw new RuntimeException("Not a shape!");
	}
}
