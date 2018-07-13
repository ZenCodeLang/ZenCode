/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import org.openzen.drawablegui.DUIContext;

/**
 *
 * @author Hoofdgebruiker
 */
public class DEmptyStylesheet implements DStylesheet {
	public static final DEmptyStylesheet INSTANCE = new DEmptyStylesheet();
	
	private DEmptyStylesheet() {}

	@Override
	public DStyleDefinition getInstance(DUIContext context) {
		return new DEmptyStyleDefinition(context);
	}
}
