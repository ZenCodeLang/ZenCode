/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DTooltip {
	void setContext(DUIContext context, DStylePath parent);
	
	int getWidth();
	
	int getHeight();
	
	void setBounds(DIRectangle bounds);
	
	DIRectangle getBounds();
	
	void paint(DCanvas canvas);
}
