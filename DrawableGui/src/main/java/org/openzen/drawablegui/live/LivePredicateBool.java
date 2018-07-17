/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

import java.util.function.Predicate;
import org.openzen.drawablegui.Destructible;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.listeners.ListenerList;

/**
 *
 * @author Hoofdgebruiker
 */
public class LivePredicateBool<T> implements LiveBool, Destructible, LiveObject.Listener<T> {
	private final ListenerList<LiveBool.Listener> listeners = new ListenerList<>();
	private final LiveObject<T> source;
	private final Predicate<T> predicate;
	private final ListenerHandle<LiveObject.Listener<T>> sourceListener;
	private boolean value;
	
	public LivePredicateBool(LiveObject<T> source, Predicate<T> predicate) {
		this.source = source;
		this.predicate = predicate;
		this.sourceListener = source.addListener(this);
		setValueInternal(predicate.test(source.getValue()));
	}
	
	@Override
	public void close() {
		sourceListener.close();
	}

	@Override
	public boolean getValue() {
		return value;
	}

	@Override
	public ListenerHandle<Listener> addListener(Listener listener) {
		return listeners.add(listener);
	}
	
	private void setValueInternal(boolean value) {
		if (value == this.value)
			return;
		
		boolean oldValue = this.value;
		this.value = value;
		listeners.accept(listener -> listener.onChanged(oldValue, value));
	}

	@Override
	public void onUpdated(T oldValue, T newValue) {
		setValueInternal(predicate.test(newValue));
	}
}
