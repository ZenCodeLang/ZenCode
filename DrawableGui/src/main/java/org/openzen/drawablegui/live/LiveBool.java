/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.listeners.ListenerList;

/**
 *
 * @author Hoofdgebruiker
 */
public class LiveBool {
	private final ListenerList<Listener> listeners = new ListenerList<>();
	private boolean value;
	
	public boolean getValue() {
		return value;
	}
	
	public void setValue(boolean value) {
		boolean oldValue = this.value;
		this.value = value;
		listeners.accept(listener -> listener.onChanged(oldValue, value));
	}
	
	public void toggle() {
		setValue(!value);
	}
	
	public ListenerHandle<Listener> addListener(Listener listener) {
		return listeners.add(listener);
	}
	
	public interface Listener {
		void onChanged(boolean oldValue, boolean newValue);
	}
}
