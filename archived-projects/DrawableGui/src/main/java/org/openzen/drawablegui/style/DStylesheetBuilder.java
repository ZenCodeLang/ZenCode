/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hoofdgebruiker
 */
public class DStylesheetBuilder {
	private final Map<String, DStyleElement> elements = new HashMap<>();

	public DStylesheetBuilder dimensionDp(String name, float value) {
		elements.put(name, new DDpDimension(value));
		return this;
	}

	public DStylesheetBuilder dimensionPx(String name, float value) {
		elements.put(name, new DPxDimension(value));
		return this;
	}

	public DStylesheetBuilder dimensionSp(String name, float value) {
		elements.put(name, new DSpDimension(value));
		return this;
	}

	public DStylesheetBuilder color(String name, int value) {
		elements.put(name, new DColorElement(value));
		return this;
	}

	public DStylesheetBuilder font(String name, DFontElement font) {
		elements.put(name, font);
		return this;
	}

	public DStylesheetBuilder shadow(String name, DShadowElement shadow) {
		elements.put(name, shadow);
		return this;
	}

	public DStylesheetBuilder border(String name, DBorderElement border) {
		elements.put(name, border);
		return this;
	}

	public DStylesheetBuilder marginDp(String name, float value) {
		elements.put(name, (DMarginElement) context -> new DMargin(context.dp(value)));
		return this;
	}

	public DStylesheetBuilder shape(String name, DShapeElement shape) {
		elements.put(name, shape);
		return this;
	}

	public DStylesheet build() {
		return new DSimpleStylesheet(elements);
	}
}
