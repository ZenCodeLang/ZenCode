/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

import java.util.Iterator;
import org.openzen.drawablegui.listeners.ListenerHandle;

/**
 *
 * @author Hoofdgebruiker
 */
public interface LiveList<T> extends Iterable<T> {
	int indexOf(T value);
	
	int size();
	
	T get(int index);
	
	void close();
	
	@Override
	Iterator<T> iterator();
	
	ListenerHandle<Listener<T>> addListener(Listener<T> listener);
	
	interface Listener<T> {
		void onInserted(int index, T value);
		
		void onChanged(int index, T oldValue, T newValue);
		
		void onRemoved(int index, T oldValue);
	}
}
