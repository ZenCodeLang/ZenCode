/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import java.util.Map;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.border.DBorder;

/**
 *
 * @author Hoofdgebruiker
 */
public class DSimpleStylesheet implements DStylesheet {
	private final Map<String, DStyleElement> elements;
	
	public DSimpleStylesheet(Map<String, DStyleElement> elements) {
		this.elements = elements;
	}

	@Override
	public DStyleDefinition getInstance(DUIContext context) {
		return new Instance(context);
	}
	
	private class Instance implements DStyleDefinition {
		private final DUIContext context;
		
		private Instance(DUIContext context) {
			this.context = context;
		}

		@Override
		public int getDimension(String name, DDimension defaultValue) {
			if (!elements.containsKey(name))
				return defaultValue.evalInt(context);
			
			return elements.get(name).asDimension().evalInt(context);
		}

		@Override
		public float getFloatDimension(String name, DDimension defaultValue) {
			if (!elements.containsKey(name))
				return defaultValue.eval(context);
			
			return elements.get(name).asDimension().eval(context);
		}

		@Override
		public int getColor(String name, int defaultValue) {
			if (!elements.containsKey(name))
				return defaultValue;
			
			return elements.get(name).asColor();
		}

		@Override
		public DShadow getShadow(String name, DShadowElement defaultValue) {
			if (!elements.containsKey(name))
				return defaultValue.eval(context);
			
			return elements.get(name).asShadow().eval(context);
		}

		@Override
		public DFont getFont(String name, DFontElement defaultValue) {
			if (!elements.containsKey(name))
				return defaultValue.eval(context);
			
			return elements.get(name).asFont().eval(context);
		}

		@Override
		public DBorder getBorder(String name, DBorderElement defaultValue) {
			if (!elements.containsKey(name))
				return defaultValue.eval(context);
			
			return elements.get(name).asBorder().eval(context);
		}
		
		@Override
		public DMargin getMargin(String name, DMarginElement defaultValue) {
			if (!elements.containsKey(name))
				return defaultValue.eval(context);
			
			return elements.get(name).asMargin().eval(context);
		}
	}
}
