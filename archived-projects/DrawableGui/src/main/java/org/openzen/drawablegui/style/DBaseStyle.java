/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import org.openzen.drawablegui.border.DBorder;
import org.openzen.drawablegui.border.DEmptyBorder;

/**
 * @author Hoofdgebruiker
 */
public class DBaseStyle {
	public final DMargin margin;
	public final DBorder border;
	public final DShadow shadow;
	public final DShape shape;
	public final int backgroundColor;

	public DBaseStyle(DStyleDefinition style) {
		border = style.getBorder("border", DEmptyBorder.ELEMENT);
		backgroundColor = style.getColor("backgroundColor", 0);
		margin = style.getMargin("margin", DMargin.EMPTY_ELEMENT);
		shape = style.getShape("shape", DRectangleShape.ELEMENT);
		shadow = style.getShadow("shadow", DShadow.NONE_ELEMENT);
	}

	public DBaseStyle(DStyleDefinition style, DBorderElement defaultBorder, int defaultBackgroundColor) {
		border = style.getBorder("border", defaultBorder);
		backgroundColor = style.getColor("backgroundColor", defaultBackgroundColor);
		margin = style.getMargin("margin", DMargin.EMPTY_ELEMENT);
		shape = style.getShape("shape", DRectangleShape.ELEMENT);
		shadow = style.getShadow("shadow", DShadow.NONE_ELEMENT);
	}

	public DBaseStyle(DStyleDefinition style, DBorderElement defaultBorder, int defaultBackgroundColor, DShapeElement defaultShape, DShadowElement defaultShadow) {
		border = style.getBorder("border", defaultBorder);
		backgroundColor = style.getColor("backgroundColor", defaultBackgroundColor);
		margin = style.getMargin("margin", DMargin.EMPTY_ELEMENT);
		shape = style.getShape("shape", defaultShape);
		shadow = style.getShadow("shadow", defaultShadow);
	}
}
