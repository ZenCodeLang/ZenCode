/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.DColorableIcon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.listeners.ListenerList;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEAspectToolbar {
	private final ListenerList<Listener> listeners = new ListenerList<>();
	
	public final int order;
	public final DColorableIcon icon;
	public final String title;
	public final String description;
	private boolean active;
	private final List<IDEAspectBarControl> controls = new ArrayList<>();
	
	public IDEAspectToolbar(int order, DColorableIcon icon, String title, String description) {
		this.order = order;
		this.icon = icon;
		this.title = title;
		this.description = description;
		this.active = true;
	}
	
	public ListenerHandle<Listener> addListener(Listener listener) {
		return listeners.add(listener);
	}
	
	public boolean getActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
		listeners.accept(listener -> listener.onActiveChanged(active));
	}
	
	public List<IDEAspectBarControl> getControls() {
		return Collections.unmodifiableList(controls);
	}
	
	public void addControl(IDEAspectBarControl control) {
		controls.add(control);
		listeners.accept(listener -> listener.onControlAdded(control));
	}
	
	public void removeControl(IDEAspectBarControl control) {
		controls.remove(control);
		listeners.accept(listener -> listener.onControlRemoved(control));
	}
	
	public interface Listener {
		void onActiveChanged(boolean active);
		
		void onControlAdded(IDEAspectBarControl control);
		
		void onControlRemoved(IDEAspectBarControl control);
	}
}
