/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import org.openzen.drawablegui.DUIContext;

/**
 * @author Hoofdgebruiker
 */
public interface DDimension extends DStyleElement {
	float eval(DUIContext context);

	default int evalInt(DUIContext context) {
		return (int) (eval(context) + 0.5f);
	}

	@Override
	default DDimension asDimension() {
		return this;
	}
}
