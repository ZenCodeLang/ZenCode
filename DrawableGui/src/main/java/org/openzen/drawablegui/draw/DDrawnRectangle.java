/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.draw;

import java.io.Closeable;
import org.openzen.drawablegui.DIRectangle;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DDrawnRectangle extends Closeable {
	void setRectangle(DIRectangle rectangle);
	
	void setColor(int color);
	
	@Override
	void close();
}
