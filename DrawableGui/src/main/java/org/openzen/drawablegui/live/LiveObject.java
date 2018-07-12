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
public interface LiveObject<T> {
	public T getValue();
	
	public ListenerHandle<Listener<T>> addListener(Listener<T> listener);
	
	public interface Listener<T> {
		void onUpdated(T oldValue, T newValue);
	}
}
