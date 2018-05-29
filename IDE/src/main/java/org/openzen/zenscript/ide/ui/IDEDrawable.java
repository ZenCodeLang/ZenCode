/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DTransform2D;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDEDrawable {
	void draw(DCanvas canvas, DTransform2D transform, IDEStyle style);
	
	float getNominalWidth();
	
	float getNominalHeight();
}
