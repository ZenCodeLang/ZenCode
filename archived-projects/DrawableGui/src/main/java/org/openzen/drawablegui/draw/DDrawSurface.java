/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.draw;

import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.style.DStyleDefinition;
import org.openzen.drawablegui.style.DStylePath;

/**
 * @author Hoofdgebruiker
 */
public interface DDrawSurface extends DDrawTarget {
	DUIContext getContext();

	DStyleDefinition getStylesheet(DStylePath path);

	DSubSurface createSubSurface(int z);
}
