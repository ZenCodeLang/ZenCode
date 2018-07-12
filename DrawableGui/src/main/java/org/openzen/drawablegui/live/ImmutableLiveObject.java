/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

import org.openzen.drawablegui.listeners.DummyListenerHandle;
import org.openzen.drawablegui.listeners.ListenerHandle;

/**
 *
 * @author Hoofdgebruiker
 */
public class ImmutableLiveObject<T> implements LiveObject<T> {
	private final T value;
	
	public ImmutableLiveObject(T value) {
		this.value = value;
	}
	
	@Override
	public T getValue() {
		return value;
	}

	@Override
	public ListenerHandle<Listener<T>> addListener(Listener<T> listener) {
		return new DummyListenerHandle<>(listener);
	}
}
