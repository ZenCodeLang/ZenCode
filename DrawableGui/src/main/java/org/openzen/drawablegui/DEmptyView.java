/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.live.ImmutableLiveObject;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DEmptyView implements DComponent {
	public static final DEmptyView INSTANCE = new DEmptyView();
	
	private static final LiveObject<DDimensionPreferences> DIMENSION = new ImmutableLiveObject(new DDimensionPreferences(0, 0));
	private static final DIRectangle NO_BOUNDS = new DIRectangle(0, 0, 0, 0);
	
	private DEmptyView() {}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		
	}
	
	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return DIMENSION;
	}
	
	@Override
	public DIRectangle getBounds() {
		return NO_BOUNDS;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		
	}

	@Override
	public void paint(DCanvas canvas) {
		
	}

	@Override
	public void close() {
		// nothing to clean up
	}
}
