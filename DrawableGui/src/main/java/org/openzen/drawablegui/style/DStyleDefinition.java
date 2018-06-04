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
public interface DStyleDefinition {
	public int getDimension(String name, DDimension defaultValue);
	
	public float getFloatDimension(String name, DDimension defaultValue);
	
	public int getColor(String name, int defaultValue);
	
	public DShadow getShadow(String name, Function<DUIContext, DShadow> defaultValue);
	
	public DFont getFont(String name, Function<DUIContext, DFont> defaultValue);
	
	public DBorder getBorder(String name, Function<DUIContext, DBorder> defaultValue);
}
