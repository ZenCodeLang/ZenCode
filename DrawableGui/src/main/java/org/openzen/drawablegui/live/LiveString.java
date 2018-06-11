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
public interface LiveString {
	ListenerHandle<Listener> addListener(Listener listener);
	
	String getValue();
	
	void setValue(String value);
	
	interface Listener {
		void onChanged(String oldValue, String newValue);
	}
}
