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
public class ImmutableLiveBool implements LiveBool {
	public static final ImmutableLiveBool TRUE = new ImmutableLiveBool(true);
	public static final ImmutableLiveBool FALSE = new ImmutableLiveBool(false);
	
	private final boolean value;
	
	private ImmutableLiveBool(boolean value) {
		this.value = value;
	}

	@Override
	public boolean getValue() {
		return value;
	}

	@Override
	public ListenerHandle<Listener> addListener(Listener listener) {
		return new DummyListenerHandle<>(listener);
	}
}
