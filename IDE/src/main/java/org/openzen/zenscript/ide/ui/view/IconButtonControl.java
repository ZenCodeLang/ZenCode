/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import java.util.function.Consumer;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.listeners.DIRectangle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class IconButtonControl implements DComponent {
	private final DDrawable icon;
	private final Consumer<DMouseEvent> onClick;
	
	private DUIContext context;
	private DIRectangle bounds;
	private final LiveObject<DDimensionPreferences> preferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	private boolean hover;
	
	public IconButtonControl(DDrawable icon, Consumer<DMouseEvent> onClick) {
		this.icon = icon;
		this.onClick = onClick;
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		preferences.setValue(new DDimensionPreferences(
				(int)(icon.getNominalWidth() * context.getScale() + 0.5f),
				(int)(icon.getNominalHeight() * context.getScale() + 0.5f)));
	}

	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return preferences;
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
		if (hover) {
			canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 0xFFE0E0E0);
		}
		icon.draw(canvas, DTransform2D.scaleAndTranslate(
				bounds.x + (bounds.width - icon.getNominalWidth() * context.getScale()) / 2,
				bounds.y + (bounds.height - icon.getNominalHeight() * context.getScale()) / 2,
				context.getScale()));
	}

	@Override
	public void close() {
		// nothing to clean up
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hover = true;
		context.repaint(bounds);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hover = false;
		context.repaint(bounds);
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		onClick.accept(e);
	}
	
	private void repaint() {
		
	}
}
