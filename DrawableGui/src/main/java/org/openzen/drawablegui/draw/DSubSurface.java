/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.draw;

import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DTransform2D;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DSubSurface extends DDrawnElement, DDrawSurface {
	@Override
	default void setTransform(DTransform2D transform) {
		setOffset((int)transform.xx, (int)transform.yy);
	}
	
	void setOffset(int x, int y);
	
	void setClip(DIRectangle bounds);
}
