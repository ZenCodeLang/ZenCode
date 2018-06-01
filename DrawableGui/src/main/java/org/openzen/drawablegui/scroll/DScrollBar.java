/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.scroll;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.listeners.DIRectangle;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveInt;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DScrollBar implements DComponent {
	private final LiveObject<DDimensionPreferences> preferences;
	
	private final DStyleClass styleClass;
	private final LiveInt targetHeight;
	private final LiveInt offset;
	
	private final ListenerHandle<LiveInt.Listener> targetHeightListener;
	private final ListenerHandle<LiveInt.Listener> offsetListener;
	
	private DUIContext context;
	private DScrollBarStyle style;
	private DIRectangle bounds;
	
	private int fromY = 0;
	private int toY = 0;
	private boolean hovering = false;
	private boolean dragging = false;
	private int dragStartOffset;
	private int dragStartY;
	
	public DScrollBar(DStyleClass styleClass, LiveInt targetHeight, LiveInt offset) {
		this.styleClass = styleClass;
		this.targetHeight = targetHeight;
		this.offset = offset;
		this.preferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
		
		targetHeightListener = targetHeight.addListener(new ScrollListener());
		offsetListener = offset.addListener(new ScrollListener());
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		this.style = new DScrollBarStyle(context.getStylesheets().get(context, parent.getChild("scrollbar", styleClass)));
		preferences.setValue(new DDimensionPreferences(style.width, 0));
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
		recalculate();
	}

	@Override
	public void paint(DCanvas canvas) {
		if (targetHeight.getValue() <= this.bounds.height)
			return; // no scrollbar
		
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.scrollBarBackgroundColor);
		
		int color = style.scrollBarNormalColor;
		if (hovering)
			color = style.scrollBarHoverColor;
		if (dragging)
			color = style.scrollBarPressColor;
		
		canvas.fillRectangle(bounds.x, fromY, this.bounds.width, toY - fromY, color);
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		checkHover(e);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		setHovering(false);
		setDragging(false);
	}
	
	@Override
	public void onMouseMove(DMouseEvent e) {
		checkHover(e);
	}
	
	@Override
	public void onMouseDrag(DMouseEvent e) {
		if (dragging) {
			int deltaY = e.y - dragStartY;
			int offsetForDelta = deltaY * targetHeight.getValue() / this.bounds.height;
			offset.setValue(dragStartOffset + offsetForDelta);
		}
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		if (e.y >= fromY && e.y < toY) {
			dragStartOffset = offset.getValue();
			dragStartY = e.y;
			setDragging(true);
		}
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		setDragging(false);
	}

	@Override
	public void close() {
		// nothing to clean up
	}
	
	private void checkHover(DMouseEvent e) {
		setHovering(e.y >= fromY && e.y < toY);
	}
	
	private void setHovering(boolean hovering) {
		if (hovering == this.hovering)
			return;
		
		this.hovering = hovering;
		context.repaint(bounds.x, fromY, bounds.width, toY - fromY);
	}
	
	private void setDragging(boolean dragging) {
		if (dragging == this.dragging)
			return;
		
		this.dragging = dragging;
		context.repaint(bounds.x, fromY, bounds.width, toY - fromY);
	}
	
	private void recalculate() {
		if (targetHeight.getValue() == 0 || bounds == null)
			return;
		
		fromY = bounds.y + this.bounds.height * offset.getValue() / targetHeight.getValue();
		toY = bounds.y + this.bounds.height * (offset.getValue() + this.bounds.height) / targetHeight.getValue();
	}
	
	private class ScrollListener implements LiveInt.Listener {
		@Override
		public void onChanged(int oldValue, int newValue) {
			recalculate();
		}
	}
}
