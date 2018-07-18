/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DShape {
	DPath instance(DIRectangle bounds);
}
