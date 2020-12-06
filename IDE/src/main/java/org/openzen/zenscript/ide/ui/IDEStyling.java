/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.style.DRoundedRectangleShape;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DShadowElement;
import org.openzen.drawablegui.style.DShapeElement;

/**
 * @author Hoofdgebruiker
 */
public class IDEStyling {
	public static final DShadowElement BLOCK_SHADOW = context -> new DShadow(0xFF888888, 0, 0.5f * context.getScale(), 3 * context.getScale());
	public static final DShapeElement BLOCK_SHAPE = context -> new DRoundedRectangleShape(context.dp(2));
	public static final int ERROR_RED = 0xFFE81123;
}
