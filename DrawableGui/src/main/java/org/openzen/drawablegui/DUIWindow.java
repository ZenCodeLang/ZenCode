/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DUIWindow {
	LiveObject<DIRectangle> getWindowBounds();
	
	DUIContext getContext();
	
	boolean hasTitleBar();
	
	void close();
	
	void maximize();
	
	void restore();
	
	void minimize();
	
	LiveObject<State> getWindowState();
	
	LiveBool getActive();
	
	void focus(DComponent component);
	
	DUIWindow openModal(String title, DComponent root);
	
	enum State {
		NORMAL,
		MAXIMIZED,
		MINIMIZED
	};
}
