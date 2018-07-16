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
public class SimpleLiveInt implements LiveInt {
	private final ListenerList<Listener> listeners = new ListenerList<>();
	private int value;

	public SimpleLiveInt(int value) {
		this.value = value;
	}
	
	@Override
	public int getValue() {
		return value;
	}

	@Override
	public void setValue(int value) {
		if (value == this.value)
			return;
		
		int oldValue = this.value;
		this.value = value;
		listeners.accept(listener -> listener.onChanged(oldValue, value));
	}

	@Override
	public ListenerHandle<Listener> addListener(Listener listener) {
		return listeners.add(listener);
	}
}
