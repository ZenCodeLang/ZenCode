/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.listeners.ListenerList;
import org.openzen.zenscript.ide.host.IDESourceFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEDockWindow {
	private final ListenerList<Listener> listeners = new ListenerList<>();
	
	public ListenerHandle<Listener> addListener(Listener listener) {
		return listeners.add(listener);
	}
	
	public void open(IDESourceFile sourceFile) {
		listeners.accept(listener -> listener.onOpen(sourceFile));
	}
	
	public interface Listener {
		void onOpen(IDESourceFile sourceFile);
	}
}
