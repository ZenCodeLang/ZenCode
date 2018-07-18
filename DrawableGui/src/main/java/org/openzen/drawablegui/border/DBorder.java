/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.border;

import java.io.Closeable;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.draw.DDrawSurface;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DBorder extends Closeable {
	public void update(DDrawSurface surface, int z, DIRectangle bounds);
	
	public int getPaddingLeft();
	
	public int getPaddingRight();
	
	public int getPaddingTop();
	
	public int getPaddingBottom();
	
	default int getPaddingHorizontal() {
		return getPaddingLeft() + getPaddingRight();
	}
	
	default int getPaddingVertical() {
		return getPaddingTop() + getPaddingBottom();
	}
	
	@Override
	void close();
}
