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
public interface LiveBool {
	public boolean getValue();
	
	public void setValue(boolean value);
	
	default void toggle() {
		setValue(!getValue());
	}
	
	public ListenerHandle<Listener> addListener(Listener listener);
	
	public interface Listener {
		void onChanged(boolean oldValue, boolean newValue);
	}
}
