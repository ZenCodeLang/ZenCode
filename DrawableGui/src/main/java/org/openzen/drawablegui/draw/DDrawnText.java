/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.draw;

import java.io.Closeable;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DDrawnText extends Closeable {
	void setPosition(float x, float y);
	
	void setColor(int color);
	
	@Override
	void close();
}
