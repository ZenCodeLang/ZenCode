/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DDrawable {
	void draw(DCanvas canvas, DTransform2D transform);
	
	float getNominalWidth();
	
	float getNominalHeight();
}
