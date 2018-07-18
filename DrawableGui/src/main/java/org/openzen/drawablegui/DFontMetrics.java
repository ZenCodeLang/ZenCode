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
public interface DFontMetrics {
	int getAscent();
	
	int getDescent();
	
	int getLeading();
	
	int getWidth(String str);
	
	int getWidth(String str, int offset, int length);
	
	default int getLineHeight() {
		return getAscent() + getDescent();
	}
}
