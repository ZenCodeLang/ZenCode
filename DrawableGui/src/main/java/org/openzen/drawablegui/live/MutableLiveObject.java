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
public interface MutableLiveObject<T> extends LiveObject<T> {
	public void setValue(T value);
}
