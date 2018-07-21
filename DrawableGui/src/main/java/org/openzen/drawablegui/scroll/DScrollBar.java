/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.scroll;

import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveInt;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStyleClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class DScrollBar implements DComponent {
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	
	private final DStyleClass styleClass;
	private final LiveInt targetHeight;
	private final LiveInt offset;
	
	private final ListenerHandle<LiveInt.Listener> targetHeightListener;
	private final ListenerHandle<LiveInt.Listener> offsetListener;
	
	private DComponentContext context;
	private DScrollBarStyle style;
	private DIRectangle bounds;
	
	private int fromY = 0;
	private int toY = 0;
	private boolean hovering = false;
	private boolean dragging = false;
	private int dragStartOffset;
	private int dragStartY;
	
	private DDrawnRectangle background;
	private DDrawnShape bar;
	
	public DScrollBar(DStyleClass styleClass, LiveInt targetHeight, LiveInt offset) {
		this.styleClass = styleClass;
		this.targetHeight = targetHeight;
		this.offset = offset;
		
		targetHeightListener = targetHeight.addListener(new ScrollListener());
		offsetListener = offset.addListener(new ScrollListener());
	}

	@Override
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("scrollbar", styleClass);
		style = context.getStyle(DScrollBarStyle::new);
		sizing.setValue(new DSizing(style.width, 0));
		
		background = context.fillRect(0, DIRectangle.EMPTY, style.scrollBarBackgroundColor);
	}
	
	@Override
	public void unmount() {
		background.close();
		background = null;
		
		if (bar != null) {
			bar.close();
			bar = null;
		}
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
		background.setRectangle(bounds);
		recalculate();
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
		targetHeightListener.close();
		offsetListener.close();
		
		unmount();
	}
	
	private void updateBarColor() {
		int color = style.scrollBarNormalColor;
		if (hovering)
			color = style.scrollBarHoverColor;
		if (dragging)
			color = style.scrollBarPressColor;		
		if (targetHeight.getValue() <= this.bounds.height)
			color = 0;
		
		bar.setColor(color);
	}
	
	private void checkHover(DMouseEvent e) {
		setHovering(e.y >= fromY && e.y < toY);
	}
	
	private void setHovering(boolean hovering) {
		if (hovering == this.hovering)
			return;
		
		this.hovering = hovering;
		updateBarColor();
	}
	
	private void setDragging(boolean dragging) {
		if (dragging == this.dragging)
			return;
		
		this.dragging = dragging;
		updateBarColor();
	}
	
	private void recalculate() {
		if (targetHeight.getValue() == 0 || bounds == null)
			return;
		
		fromY = bounds.y + this.bounds.height * offset.getValue() / targetHeight.getValue();
		toY = bounds.y + this.bounds.height * (offset.getValue() + this.bounds.height) / targetHeight.getValue();
		
		if (bar != null)
			bar.close();
		bar = context.fillPath(1, DPath.rectangle(bounds.x, fromY, bounds.width, toY - fromY), DTransform2D.IDENTITY, style.scrollBarNormalColor);
		updateBarColor();
	}
	
	private class ScrollListener implements LiveInt.Listener {
		@Override
		public void onChanged(int oldValue, int newValue) {
			recalculate();
		}
	}
}
