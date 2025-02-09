/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DUIContext;

/**
 * @author Hoofdgebruiker
 */
@FunctionalInterface
public interface DFontElement extends DStyleElement {
	DFont eval(DUIContext context);

	@Override
	default DFontElement asFont() {
		return this;
	}
}
