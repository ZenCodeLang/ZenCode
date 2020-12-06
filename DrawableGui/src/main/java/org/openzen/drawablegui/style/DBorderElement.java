/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.border.DBorder;

/**
 * @author Hoofdgebruiker
 */
public interface DBorderElement extends DStyleElement {
	DBorder eval(DUIContext context);

	@Override
	default DBorderElement asBorder() {
		return this;
	}
}
