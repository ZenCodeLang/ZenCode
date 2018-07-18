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
public class SimpleLiveObject<T> implements MutableLiveObject<T> {
	private final ListenerList<Listener<T>> listeners = new ListenerList<>();
	private T value;
	
	public SimpleLiveObject(T value) {
		this.value = value;
	}
	
	@Override
	public T getValue() {
		return value;
	}
	
	@Override
	public void setValue(T value) {
		if (value == this.value)
			return;
		
		T oldValue = this.value;
		this.value = value;
		listeners.accept(listener -> listener.onUpdated(oldValue, value));
	}
	
	@Override
	public ListenerHandle<Listener<T>> addListener(Listener<T> listener) {
		return listeners.add(listener);
	}
}
