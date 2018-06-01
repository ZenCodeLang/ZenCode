/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import java.io.Closeable;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.listeners.ListenerList;
import org.openzen.drawablegui.live.LiveArrayList;
import org.openzen.drawablegui.live.LiveList;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEAspectBar {
	public final LiveList<IDEAspectToolbar> aspectToolbars = new LiveArrayList<>(); // TODO: only expose read-only variant
	private final ListenerList<Listener> listeners = new ListenerList<>();
	
	public ListenerHandle<Listener> addListener(Listener listener) {
		return listeners.add(listener);
	}
	
	public void addToolbar(IDEAspectToolbar toolbar) {
		int index = insertToolbar(toolbar);
		listeners.accept(listener -> listener.onAspectBarAdded(index, toolbar));
	}
	
	public boolean removeToolbar(IDEAspectToolbar toolbar) {
		int index = aspectToolbars.indexOf(toolbar);
		if (index < 0)
			return false;
		
		aspectToolbars.remove(index);
		listeners.accept(listener -> listener.onAspectBarRemoved(index, toolbar));
		return true;
	}
	
	public BarHandle openContentBar(IDEAspectToolbar toolbar) {
		listeners.accept(listener -> listener.onOpenContextBar(toolbar));
		return new BarHandle(toolbar);
	}
	
	private int insertToolbar(IDEAspectToolbar toolbar) {
		for (int i = 0; i < aspectToolbars.size(); i++) {
			if (toolbar.order < aspectToolbars.get(i).order) {
				aspectToolbars.add(i, toolbar);
				return i;
			}
		}
		int index = aspectToolbars.size();
		aspectToolbars.add(toolbar);
		return index;
	}
	
	public class BarHandle implements Closeable {
		private final IDEAspectToolbar toolbar;
		
		public BarHandle(IDEAspectToolbar toolbar) {
			this.toolbar = toolbar;
		}
		
		@Override
		public void close() {
			listeners.accept(listener -> listener.onCloseContextBar(toolbar));
		}
	}
	
	public interface Listener {
		void onAspectBarAdded(int index, IDEAspectToolbar toolbar);
		void onAspectBarRemoved(int index, IDEAspectToolbar toolbar);
		void onOpenContextBar(IDEAspectToolbar toolbar);
		void onCloseContextBar(IDEAspectToolbar toolbar);
	}
}
