/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DRectangleShape implements DShape {
	public static final DRectangleShape INSTANCE = new DRectangleShape();
	public static final DShapeElement ELEMENT = context -> INSTANCE;

	@Override
	public DPath instance(DIRectangle bounds) {
		return DPath.rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
	}
}
