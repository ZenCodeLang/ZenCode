/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.listeners.ListenerList;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEStatusBar {
	private final ListenerList<Listener> listeners = new ListenerList<>();
	private String message;
	
	public ListenerHandle<Listener> addListener(Listener listener) {
		return listeners.add(listener);
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
		listeners.accept(listener -> listener.onMessageUpdated(message));
	}
	
	public interface Listener {
		void onMessageUpdated(String message);
	}
}
