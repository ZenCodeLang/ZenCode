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
public class SimpleLiveBool implements MutableLiveBool {
	private final ListenerList<LiveBool.Listener> listeners = new ListenerList<>();
	private boolean value;
	
	public SimpleLiveBool() {
		this(false);
	}
	
	public SimpleLiveBool(boolean value) {
		this.value = value;
	}
	
	@Override
	public boolean getValue() {
		return value;
	}
	
	@Override
	public void setValue(boolean value) {
		if (value == this.value)
			return;
		
		boolean oldValue = this.value;
		this.value = value;
		listeners.accept(listener -> listener.onChanged(oldValue, value));
	}
	
	@Override
	public ListenerHandle<LiveBool.Listener> addListener(LiveBool.Listener listener) {
		return listeners.add(listener);
	}
}
