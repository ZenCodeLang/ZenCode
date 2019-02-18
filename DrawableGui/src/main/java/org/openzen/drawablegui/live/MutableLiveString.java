/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

import live.LiveString;

/**
 *
 * @author Hoofdgebruiker
 */
public interface MutableLiveString extends LiveString {
	void setValue(String value);
}
