/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.draw.DDrawTarget;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DColorableIcon {
	void draw(DDrawTarget target, int z, DTransform2D transform, int color);
	
	float getNominalWidth();
	
	float getNominalHeight();
}
