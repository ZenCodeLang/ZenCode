/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

import org.openzen.drawablegui.listeners.ListenerHandle;

/**
 *
 * @author Hoofdgebruiker
 */
public interface LiveInt {
	int getValue();
	
	void setValue(int value);
	
	ListenerHandle<Listener> addListener(Listener listener);
	
	interface Listener {
		void onChanged(int oldValue, int newValue);
	}
}
