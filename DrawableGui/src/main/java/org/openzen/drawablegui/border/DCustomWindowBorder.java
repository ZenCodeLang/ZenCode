/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.border;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.ImmutableLiveObject;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DCustomWindowBorder implements DComponent {
	private final DStyleClass styleClass;
	private final DComponent content;
	private final LiveObject<DSizing> sizing = new ImmutableLiveObject<>(DSizing.EMPTY);
	
	private DUIContext context;
	private DCustomWindowBorderStyle style;
	private DIRectangle bounds;
	private DPath border;
	
	private LiveBool active;
	private LiveObject<DUIWindow.State> state;
	
	private ListenerHandle<LiveObject.Listener<DUIWindow.State>> stateListener;
	private ListenerHandle<LiveBool.Listener> activeListener;
	
	public DCustomWindowBorder(DStyleClass styleClass, DComponent content) {
		this.styleClass = styleClass;
		this.content = content;
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		content.setContext(parent, context);
		
		state = context.getWindow().getWindowState();
		active = context.getWindow().getActive();
		stateListener = state.addListener(this::onStateChanged);
		activeListener = active.addListener(this::onActiveChanged);
		
		DStylePath path = parent.getChild("customwindowborder", styleClass);
		style = new DCustomWindowBorderStyle(context.getStylesheets().get(context, path));
		
		if (bounds != null)
			layout();
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
		int contentBaseline = content.getBaselineY();
		return contentBaseline == -1 ? -1 : style.padding + style.borderWidth + contentBaseline;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		layout();
	}
	
	private void layout() {	
		if (style == null)
			return;
		
		if (state.getValue() != DUIWindow.State.MAXIMIZED) {
			border = DPath.rectangle(
				bounds.x + style.padding,
				bounds.y + style.padding,
				bounds.width - 2 * style.padding - style.borderWidth,
				bounds.height - 2 * style.padding - style.borderWidth);
			
			int spacing = style.borderWidth + style.padding;
			DIRectangle inner = new DIRectangle(
					bounds.x + spacing,
					bounds.y + spacing,
					bounds.width - 2 * spacing,
					bounds.height - 2 * spacing);

			content.setBounds(inner);
		} else {
			content.setBounds(bounds);
		}
		context.repaint(bounds);
	}

	@Override
	public void paint(DCanvas canvas) {
		if (state.getValue() != DUIWindow.State.MAXIMIZED) {
			int spacing = style.borderWidth + style.padding;
			DIRectangle canvasBounds = canvas.getBounds();
			if (canvasBounds == null
					|| canvasBounds.x < bounds.x + spacing
					|| canvasBounds.y < bounds.y + spacing
					|| canvasBounds.x + canvasBounds.width > bounds.x + bounds.width - spacing
					|| canvasBounds.y + canvasBounds.height > bounds.y + bounds.height - spacing) {
				canvas.shadowPath(border, DTransform2D.IDENTITY, style.backgroundColor, style.shadow);
				canvas.strokePath(
						border,
						DTransform2D.IDENTITY,
						active.getValue() ? style.focusedBorderColor : style.inactiveBorderColor,
						style.borderWidth);
			}
		} else {
			canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		}
		
		content.paint(canvas);
	}

	@Override
	public void onMouseEnter(DMouseEvent e) {
		content.onMouseEnter(e);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		content.onMouseExit(e);
	}
	
	@Override
	public void onMouseMove(DMouseEvent e) {
		content.onMouseMove(e);
	}
	
	@Override
	public void onMouseDrag(DMouseEvent e) {
		content.onMouseDrag(e);
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		content.onMouseClick(e);
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		content.onMouseDown(e);
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		content.onMouseRelease(e);
	}
	
	@Override
	public void onMouseScroll(DMouseEvent e) {
		content.onMouseScroll(e);
	}
	
	@Override
	public void close() {
		
	}
	
	private void onStateChanged(DUIWindow.State oldValue, DUIWindow.State newValue) {
		layout();
	}
	
	private void onActiveChanged(boolean oldValue, boolean newValue) {
		context.repaint(bounds);
	}
}
