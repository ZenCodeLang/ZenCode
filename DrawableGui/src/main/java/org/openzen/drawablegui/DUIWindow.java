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
	boolean hasTitleBar();
	
	void close();
	
	void maximize();
	
	void restore();
	
	void minimize();
	
	LiveObject<State> getWindowState();
	
	LiveBool getActive();
	
	DUIWindow openModal(String title, DComponent root);
	
	enum State {
		NORMAL,
		MAXIMIZED,
		MINIMIZED
	};
}
