/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.listeners;

/**
 *
 * @author Hoofdgebruiker
 */
public class DummyListenerHandle<T> implements ListenerHandle<T> {
	private final T listener;

	public DummyListenerHandle(T listener) {
		this.listener = listener;
	}

	@Override
	public T getListener() {
		return listener;
	}

	@Override
	public void close() {

	}
}
