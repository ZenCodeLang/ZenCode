/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.draw;

import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.Destructible;

/**
 * @author Hoofdgebruiker
 */
public interface DDrawnElement extends Destructible {
	void setTransform(DTransform2D transform);

	DIRectangle getBounds();
}
