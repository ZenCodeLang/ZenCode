/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import java.util.ArrayList;
import java.util.List;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.listeners.ListenerList;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEAspectBar {
	private final List<IDEAspectToolbar> aspectToolbars = new ArrayList<>();
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
	
	public interface Listener {
		void onAspectBarAdded(int index, IDEAspectToolbar toolbar);
		void onAspectBarRemoved(int index, IDEAspectToolbar toolbar);
	}
}
