/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.aspectbar;

import java.util.function.Consumer;
import java.util.function.Function;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStylePath;
import org.openzen.zenscript.ide.ui.icons.ScalableCloseIcon;

/**
 *
 * @author Hoofdgebruiker
 */
public class WindowActionButton implements DComponent {
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	
	private final Function<Float, DColorableIcon> scalableIcon;
	private final Consumer<DMouseEvent> action;
	
	private LiveBool windowFocused;
	private ListenerHandle<LiveBool.Listener> windowFocusedListener;
	
	private DColorableIcon icon;
	private DIRectangle bounds;
	private boolean hover;
	private boolean press;
	private DDrawSurface surface;
	
	public WindowActionButton(Function<Float, DColorableIcon> icon, Consumer<DMouseEvent> action) {
		this.scalableIcon = icon;
		this.action = action;
	}

	@Override
	public void setSurface(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		
		windowFocused = surface.getContext().getWindow().getActive();
		windowFocusedListener = windowFocused.addListener((a, b) -> repaint());
		
		icon = scalableIcon == null ? null : scalableIcon.apply(surface.getScale());
		sizing.setValue(new DSizing(
				(int)(48 * surface.getScale()),
				(int)(24 * surface.getScale())));
	}

	@Override
	public LiveObject<DSizing> getSizing() {
		return sizing;
	}

	@Override
	public DIRectangle getBounds() {
		return bounds;
	}
	
	@Override
	public int getBaselineY() {
		return -1;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public void paint(DCanvas canvas) {
		int color = 0xFFFFFFFF;
		int iconColor = windowFocused.getValue() ? 0xFF000000 : 0xFF999999;
		
		if (hover) {
			if (icon instanceof ScalableCloseIcon) {
				color = 0xFFE81123;
				iconColor = 0xFFFFFFFF;
			} else {
				color = 0xFFE0E0E0;
			}
		}
		if (press)
			color = 0xFFCCCCCC;
		
		
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, color);
		
		if (scalableIcon != null) {
			int iconX = bounds.x + (int)(bounds.width - icon.getNominalWidth()) / 2;
			int iconY = bounds.y + (int)(bounds.height - icon.getNominalHeight()) / 2;
			icon.draw(canvas, DTransform2D.translate(iconX, iconY), iconColor);
		}
	}

	@Override
	public void close() {
		windowFocusedListener.close();
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hover = true;
		repaint();
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hover = false;
		press = false;
		repaint();
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		press = true;
		repaint();
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		if (press)
			action.accept(e);
		
		press = false;
		repaint();
	}
	
	private void repaint() {
		if (surface == null)
			return;
		
		surface.repaint(bounds);
	}
}
