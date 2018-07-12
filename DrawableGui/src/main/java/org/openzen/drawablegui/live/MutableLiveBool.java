/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

/**
 *
 * @author Hoofdgebruiker
 */
public interface MutableLiveBool extends LiveBool {
	void setValue(boolean value);
	
	default void toggle() {
		setValue(!getValue());
	}
}
