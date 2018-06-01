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
import org.openzen.drawablegui.dimension.DDimension;

/**
 *
 * @author Hoofdgebruiker
 */
public class DEmptyStyleDefinition implements DStyleDefinition {
	private final DUIContext context;
	
	public DEmptyStyleDefinition(DUIContext context) {
		this.context = context;
	}

	@Override
	public int getDimension(String name, DDimension defaultValue) {
		return defaultValue.evalInt(context);
	}
	
	@Override
	public float getFloatDimension(String name, DDimension defaultValue) {
		return defaultValue.eval(context);
	}

	@Override
	public int getColor(String name, int defaultValue) {
		return defaultValue;
	}

	@Override
	public DFont getFont(String name, Function<DUIContext, DFont> defaultValue) {
		return defaultValue.apply(context);
	}

	@Override
	public DBorder getBorder(String name, Function<DUIContext, DBorder> defaultValue) {
		return defaultValue.apply(context);
	}
}
