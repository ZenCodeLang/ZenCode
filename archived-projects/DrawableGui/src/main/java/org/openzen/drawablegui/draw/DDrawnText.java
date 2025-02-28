/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.draw;

import org.openzen.drawablegui.DTransform2D;

/**
 * @author Hoofdgebruiker
 */
public interface DDrawnText extends DDrawnColorableElement {
	@Override
	default void setTransform(DTransform2D transform) { // only uses position, not scaling or rotation
		setPosition(transform.xx, transform.yy);
	}

	void setPosition(float x, float y);
}
