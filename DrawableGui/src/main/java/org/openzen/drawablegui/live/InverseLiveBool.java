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
public class InverseLiveBool implements LiveBool {
	private final LiveBool source;
	
	public InverseLiveBool(LiveBool source) {
		this.source = source;
	}

	@Override
	public boolean getValue() {
		return !source.getValue();
	}

	@Override
	public ListenerHandle<Listener> addListener(Listener listener) {
		return source.addListener((oldValue, newValue) -> listener.onChanged(!oldValue, !newValue));
	}
}
