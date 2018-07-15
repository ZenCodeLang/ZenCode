/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.layout;

import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DSideLayoutStyle {
	public final int backgroundColor;
	
	public DSideLayoutStyle(DStyleDefinition style) {
		backgroundColor = style.getColor("backgroundColor", 0);
	}
}
