/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.border;

import org.openzen.drawablegui.DRectangle;
import org.openzen.drawablegui.DCanvas;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DBorder {
	public void paint(DCanvas canvas, DRectangle bounds);
	
	public int getPaddingLeft();
	
	public int getPaddingRight();
	
	public int getPaddingTop();
	
	public int getPaddingBottom();
}
