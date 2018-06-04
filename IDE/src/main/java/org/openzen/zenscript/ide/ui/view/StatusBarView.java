/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class StatusBarView implements DComponent {
	private static final int COLOR = 0xFFF0F0F0;
	//private static final int COLOR = 0xFFFFFFFF;
	
	private final SimpleLiveObject<DDimensionPreferences> dimensionPreferences = new SimpleLiveObject<>(new DDimensionPreferences(0, 40));
	private DIRectangle bounds;
	private DUIContext context;

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
	}
	
	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return dimensionPreferences;
	}
	
	@Override
	public DIRectangle getBounds() {
		return bounds;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public void paint(DCanvas canvas) {
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, COLOR);
	}

	@Override
	public void close() {
		// nothing to clean up
	}
}
