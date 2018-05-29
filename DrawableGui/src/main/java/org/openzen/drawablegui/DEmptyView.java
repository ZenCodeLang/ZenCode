/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.live.ImmutableLiveObject;
import org.openzen.drawablegui.live.LiveObject;

/**
 *
 * @author Hoofdgebruiker
 */
public class DEmptyView implements DComponent {
	public static final DEmptyView INSTANCE = new DEmptyView();
	
	private static final LiveObject<DDimensionPreferences> DIMENSION = new ImmutableLiveObject(new DDimensionPreferences(0, 0));
	private static final DRectangle NO_BOUNDS = new DRectangle(0, 0, 0, 0);
	
	private DEmptyView() {}

	@Override
	public void setContext(DDrawingContext context) {
		
	}
	
	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return DIMENSION;
	}
	
	@Override
	public DRectangle getBounds() {
		return NO_BOUNDS;
	}

	@Override
	public void setBounds(DRectangle bounds) {
		
	}

	@Override
	public void paint(DCanvas canvas) {
		
	}
}
