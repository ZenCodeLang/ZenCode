/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.listeners.ListenerHandle;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDEAspectBarControl extends IDEDrawable {
	ListenerHandle<InvalidationListener> addInvalidationListener(InvalidationListener listener);
	
	public interface InvalidationListener {
		/* Indicates that this component needs to be redrawn */
		void invalidateView(int x, int y, int width, int height);
		
		/* Indicates that layout should be recalculated for this component (eg because its size changed). */
		void invalidateLayout();
	}
}
