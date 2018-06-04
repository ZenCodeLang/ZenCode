/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DTransform2D;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DColorableIcon {
	void draw(DCanvas canvas, DTransform2D transform, int color);
	
	float getNominalWidth();
	
	float getNominalHeight();
}
