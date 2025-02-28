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
public class DEmptyStylesheets implements DStyleSheets {
	public static final DEmptyStylesheets INSTANCE = new DEmptyStylesheets();

	@Override
	public DStyleDefinition get(DUIContext context, DStylePath path) {
		return path.getInline(context);
	}
}
