/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.draw;

import java.io.Closeable;
import org.openzen.drawablegui.DTransform2D;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DDrawnShape extends Closeable {
	public void setTransform(DTransform2D transform);
	
	public void setColor(int color);
	
	@Override
	public void close();
}
