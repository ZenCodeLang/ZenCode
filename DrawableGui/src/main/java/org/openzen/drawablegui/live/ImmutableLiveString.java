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
public class ImmutableLiveString implements LiveString {
	private final String value;

	public ImmutableLiveString(String value) {
		this.value = value;
	}
	
	@Override
	public ListenerHandle<Listener> addListener(Listener listener) {
		return new DummyListenerHandle<>(listener);
	}

	@Override
	public String getValue() {
		return value;
	}
}
