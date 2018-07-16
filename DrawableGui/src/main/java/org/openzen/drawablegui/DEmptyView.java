/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.live.ImmutableLiveObject;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DEmptyView implements DComponent {
	public static final DEmptyView INSTANCE = new DEmptyView();
	
	private static final LiveObject<DSizing> DIMENSION = new ImmutableLiveObject(DSizing.EMPTY);
	
	private DEmptyView() {}

	@Override
	public void setSurface(DStylePath parent, int z, DDrawSurface surface) {
		
	}
	
	@Override
	public LiveObject<DSizing> getSizing() {
		return DIMENSION;
	}
	
	@Override
	public DIRectangle getBounds() {
		return DIRectangle.EMPTY;
	}

	@Override
	public int getBaselineY() {
		return -1;
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
